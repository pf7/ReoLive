package reolive

import org.singlespaced.d3js.Ops._
import D3Lib.GraphsToJS
import org.scalajs.dom
import dom.{EventTarget, MouseEvent, html}
import org.scalajs.dom.raw.KeyboardEvent
import org.singlespaced.d3js
import org.singlespaced.d3js.{Selection, d3}
import preo.frontend.{Eval, Show, Simplify}
import preo.common.TypeCheckException
import preo.backend.{Graph, Springy}
import preo.DSL
import preo.lang.Parser

import scala.scalajs.js.{JavaScriptException, UndefOr}
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._


/**
  * Created by jose on 27/04/2017.
  */
@JSExport
object WebReo extends{

  private val buttons = Seq(
    "writer"->"writer", "reader"->"reader",
    "fifo"->"fifo",     "merger"->"merger",
    "dupl"->"dupl",     "drain"->"drain",
    "(fifo*writer) & drain"->"(fifo*writer) & drain",
    "\\x . ((fifo^x)*writer) & (drain^3)" -> "\\x . ((fifo^x)*writer) & (drain^3)",
    "(\\x.fifo^x) & (\\n.drain^n)" -> "(\\x.fifo^x) & (\\n.drain^n)",
    "\\b:B . (b? fifo + dupl) & merger" -> "\\b:B . (b? fifo + dupl) & merger",
    "(\\x .drain^(x-1)) 3" -> "(\\x .drain^(x-1)) 3",
    ".. & merger!" -> "(writer^8) & merger! & merger! & reader!",
    "x=..;y=..;x&y" -> "x = lossy * fifo ; y = merger; x & y",
    "exrouter=.."->"writer & dupl & (dupl*id) & (((lossy*lossy) & (dupl*dupl) & (id*swap*id) & (id*id*merger))*id) & (id*id*drain) & (reader^2)",
    "zip=.."-> """zip =
  \n.Tr_((2*n)*(n-1))
  (((((id^(n-x))*(sym(1,1)^x))*(id^(n-x)))^(x<--n))&
   sym((2*n)*(n-1),2*n));
zip 3""",
    "unzip=.." -> """unzip =
  \n.Tr_((2*n)*(n - 1))
  (((((id^(x+1))*(sym(1,1)^((n-x)-1)))*(id^(x+1)))^(x<--n))&
   sym((2*n)*(n-1),2*n));
unzip 3""",
    "sequencer=.."-> """zip =
  \n.Tr_((2*n)*(n-1))
  (((((id^(n-x))*(sym(1,1)^x))*(id^(n-x)))^(x<--n))&
   sym((2*n)*(n-1),2*n));

unzip =
  \n.Tr_((2*n)*(n - 1))
  (((((id^(x+1))*(sym(1,1)^((n-x)-1)))*(id^(x+1)))^(x<--n))&
   sym((2*n)*(n-1),2*n));

sequencer =
  \n.(((dupl^n)&unzip(n:I)) *
    Tr_n(sym(n-1,1)&((fifofull&dupl)*((fifo & dupl)^(n-1)))&
         unzip(n:I)))&
    ((id^n)*((zip(n:I)) & (drain^n)));

(writer^3) & sequencer 3 & (reader^3)"""
  )


  @JSExport
  def main(content: html.Div) = {

    // add header
    d3.select(content).append("div")
      .attr("id", "header")
      .append("h1").text("Build Reo Families")

    val contentDiv = d3.select(content).append("div")
      .attr("id", "content")

    val rowDiv = contentDiv.append("div")
      .attr("class", "row")

    val colDiv1 = rowDiv.append("div")
      .attr("class", "col-sm-3")

    // add InputArea
    val inputDiv = colDiv1.append("div")
      .attr("id", "textBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("rows", "10")
      .attr("style", "width: 100%")
      .attr("placeholder", "press Shift+Enter to update")

    val outputBox = colDiv1.append("div")
      .attr("id", "outputBox")

    val buttonsDiv = colDiv1.append("div")
      .attr("id", "buttons")

    buttonsDiv
      .style("display:block; padding:2pt")


    val mcrl2Box = colDiv1.append("div")
      .attr("id", "mcrl2Box")
      .style("margin-top", "4px")
      .style("border", "1px solid black")
        .text("coiso")

    val svgDiv = rowDiv.append("div")
      .attr("class", "col-sm-8")

    appendSvg(svgDiv)

    fgenerate("dupl & (fifo * lossy)",outputBox)

    /**
    Will evaluate the expression being written in the input box
      */
//      inputBox.onkeyup = (e: dom.Event) => {
//        fgenerate(inputBox.value,outputBox,canvasDiv)
//      }

    val inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {(e: dom.KeyboardEvent) =>
      if(e.keyCode.toInt == 13 && e.shiftKey) fgenerate(inputAreaDom.value,outputBox)
      else ()
    }

    //inputArea.on("keyup", {(e: EventTarget, a: Int, b:UndefOr[Int]) =>println(e);fgenerate(inputAreaDom.value,outputBox)} : inputArea.DatumFunction[Unit])



    for (ops <- buttons ) yield genButton(ops,buttonsDiv, inputArea,outputBox, inputAreaDom)

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def fgenerate(input:String,outputInfo:Selection[dom.EventTarget]): Unit={
    // clear output



    outputInfo.text("")

    // update output and run script
    DSL.parseWithError(input) match {
      case Parser.Success(result, _) =>
        try {
          outputInfo.append("p")
            .text("[ "+Show(DSL.unsafeTypeOf(result))+" ]")
          Eval.unsafeInstantiate(result) match {
            case Some(reduc) =>
              // GOT A TYPE
              outputInfo.append("p")
                .text(Show(reduc)+":\n  "+
                  Show(DSL.unsafeTypeOf(result)))
              //println(Graph.toString(Graph(Eval.unsafeReduce(reduc))))
              scalajs.js.eval(GraphsToJS(Graph(Eval.unsafeReduce(reduc))))
              //mudar esta linha para utilizar d3 com novo grafo
              //e parametros em scala.js
            case _ =>
              // Failed to simplify
              outputInfo.append("p")
                .text("Failed to reduce connector: "+Show(Simplify.unsafe(result)))
          }
        }
        catch {
          // type error
          case e: TypeCheckException => outputInfo.append("p").text(Show(result)+" - Type error: " + e.getMessage)
          case e: JavaScriptException => outputInfo.append("p").text(Show(result)+" - JavaScript error : "+e+" - "+e.getClass)
        }
        // parse error
      case f: Parser.NoSuccess => outputInfo.append("p").text("Parser error: " + f.msg)
    }


  }


  private def genButton(ss:(String,String),buttonsDiv:Selection[dom.EventTarget], inputBox:Selection[dom.EventTarget],outputInfo:Selection[dom.EventTarget], inputAreaDom: html.TextArea): Unit = {
    val button = buttonsDiv.append("button")
        .text(ss._1)

    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
      inputAreaDom.value = ss._2
      fgenerate(ss._2,outputInfo)
    }} : button.DatumFunction[Unit])

  }


  private def appendSvg(div: Selection[dom.EventTarget]) = {
    val svg = div.append("svg")
      .attr("width", "600")
      .attr("height", "450")
      .style("border", "black")
      .style("border-width", "thin")
      .style("border-style", "solid")
      .style("margin", "auto")

    svg.append("g")
      .attr("class", "nodes");

    svg.append("g")
      .attr("class", "links");

    svg.append("g")
      .attr("class", "labels");

    svg.append("g")
      .attr("class", "paths");

    //inserting regular arrow at the end
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowout")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",13)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",5)
      .attr("markerHeight",5)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L 10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    //arrowhead inverted for sync drains
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowin")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",14)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",5)
      .attr("markerHeight",5)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 10,-5 L 0 ,0 L 10,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowout")
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-14)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",8)
      .attr("markerHeight",8)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L -10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowin")
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-14)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",8)
      .attr("markerHeight",8)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M -10,-5 L 0 ,0 L -10,5")
      .attr("fill", "#000")
      .style("stroke","none");
  }

}

