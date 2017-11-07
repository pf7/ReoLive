package reolive

import D3Lib.GraphsToJS
import org.scalajs.dom
import dom.{MouseEvent, html}
import org.singlespaced.d3js.d3
import preo.frontend.{Eval, Show, Simplify}
import preo.common.TypeCheckException
import preo.backend.{Graph, Springy}
import preo.DSL
import preo.lang.Parser
import preo.ast.Type

import scala.scalajs.js.JavaScriptException
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._


/**
  * Created by jose on 27/04/2017.
  */
@JSExport
object WebReo extends{

  private val canvasBox = canvas(
    style:="border: 'black'; border-width: thin;border-style: solid;margin: auto;",
//    width:="600px",
//    height:="450px",
    attr("width"):="600px",
    attr("height"):="450px",
    id:="springydemo"
  )


  @JSExport
  def main(content: html.Div) = {

//      val inputBox = input(
//        `type`:="text",
//        placeholder:="Type Here!",
//        width:="400px"
//      ).render
//val inputArea = textarea(rows:="10",cols:="36",placeholder:="dupl & (fifo * lossy)").render
    val inputArea = textarea(rows:="10",width:="100%",placeholder:="dupl & (fifo * lossy)").render
    val outputBox = div.render

    val canvasDiv = div(`class`:="col-sm-5",canvasBox).render

    val svgDiv = div.render

    appendSvg(svgDiv)
    fgenerate("dupl & (fifo * lossy)",outputBox,canvasDiv)

    /**
    Will evaluate the expression being written in the input box
      */
//      inputBox.onkeyup = (e: dom.Event) => {
//        fgenerate(inputBox.value,outputBox,canvasDiv)
//      }
    inputArea.onkeyup = (e: dom.Event) => {
      fgenerate(inputArea.value,outputBox,canvasDiv)
    }


    val buttons = div( style:="display:block; padding:2pt", //ul(
      for (ops <- Seq(
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
      )) yield genButton(ops,inputArea,outputBox,canvasDiv)
    ).render

    val header = div(id:="header",h1("Build Reo Families"))


    val contentDiv = div(
      id:="content",
//        p(
//          "Write the structure you want to see: "
//        ),
//        div(id:="inputBox",marginBottom:="2pt",inputBox),
      div(`class`:="row",
        div(`class`:="col-sm-3",
          div(id:="textBox",inputArea),
          div(id:="outputBox",outputBox),
          div(id:="buttons",buttons)
        ),
        div(`class` := "row",
        canvasDiv,
        svgDiv
        )
      )
    )

    content.appendChild(header.render)
    content.appendChild(contentDiv.render)

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def fgenerate(input:String,outputInfo:html.Div,canvas:html.Div): Unit ={
    // clear output
    outputInfo.innerHTML = ""

    // update output and run script
    DSL.parse(input) match {
      case Parser.Success(result, _) =>
        try {
          outputInfo.appendChild(genType("[ "+Show(DSL.unsafeTypeOf(result))+" ]"))
          Eval.unsafeInstantiate(result) match {
            case Some(reduc) =>
              // GOT A TYPE
              outputInfo.appendChild(genType(Show(reduc)+":\n  "+
                                             Show(DSL.unsafeTypeOf(result))))
              clearCanvas(canvas)
//              outputInfo.appendChild(genError(Springy.script(reduc)))
              scalajs.js.eval(Springy.script(Eval.unsafeReduce(reduc)))
              println(Eval.unsafeReduce(reduc))
              scalajs.js.eval(GraphsToJS(Graph(Eval.unsafeReduce(reduc))))
              //mudar esta linha para utilizar d3 com novo grafo
              //e parametros em scala.js
            case _ =>
              // Failed to simplify
              outputInfo.appendChild(genError("Failed to reduce connector: "+Show(Simplify.unsafe(result))))
          }
        }
        catch {
          // type error
          case e: TypeCheckException => outputInfo.appendChild(genError(Show(result)+" - Type error: " + e.getMessage))
          case e: JavaScriptException => outputInfo.appendChild(genError(Show(result)+" - JavaScript error : "+e.getMessage+" - "+e.getClass))
        }
        // parse error
      case f: Parser.NoSuccess => outputInfo.appendChild(genError("Parser error: " + f))
    }


  }

  private def clearCanvas(c:html.Div) = {
    c.innerHTML = ""
    c.appendChild(canvasBox.render)
  }

  private def genType(s:String): html.Paragraph =
    p(s).render
  private def genError(s:String): html.Paragraph =
    p(s).render

  private def genButton(ss:(String,String),inputBox:html.TextArea,outputInfo:html.Div,canvas:html.Div): html.Button = {
    val b = button(ss._1).render
    b.onclick = (_:MouseEvent) => {
      inputBox.value = ss._2
      fgenerate(ss._2,outputInfo,canvas)
    }
    b
  }

  private def appendSvg(div: html.Div) = {
    val svg = d3.select(div).append("svg")
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
      .attr("id","arrowhead")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",15)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",6)
      .attr("markerHeight",6)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L 10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    //arrowhead inverted for sync drains
    svg.append("defs")
      .append("marker")
      .attr("id","invertedarrowhead")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",15)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",6)
      .attr("markerHeight",6)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 10,-5 L 0 ,0 L 10,5")
      .attr("fill", "#000")
      .style("stroke","none");
  }
}

