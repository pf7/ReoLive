package reolive

import D3Lib.CopytoClipboard._
import D3Lib.GraphsToJS
import org.scalajs.dom
import dom.{EventTarget, html}
import org.singlespaced.d3js.{Selection, d3}
import preo.frontend.{Eval, Show, Simplify}
import preo.common.TypeCheckException
import preo.backend.Graph
import preo.DSL
import preo.ast.BVal
import preo.modelling.Mcrl2Program

import scala.scalajs.js.{JavaScriptException, UndefOr}
import scalajs.js.annotation.JSExportTopLevel
import scalatags.JsDom.all._


/**
  * Created by jose on 27/04/2017.
  */
object WebReo extends{

  type Block = Selection[dom.EventTarget]
  var width = 700
  var height = 500
  val density = 0.5 // nodes per 100x100 px

  private val buttons = Seq(
    "writer"->"writer", "reader"->"reader",
    "fifo"->"fifo",     "merger"->"merger",
    "dupl"->"dupl",     "drain"->"drain",
    "fifo*writer ; drain"->"fifo*writer ; drain",
    "\\x . fifo^x*writer ; drain^3" -> "\\x . fifo^x*writer ; drain^3",
    "(\\x.fifo^x) ; (\\n.drain^n)" -> "(\\x.fifo^x) ; (\\n.drain^n)",
//    "\\b:B . (b? fifo + dupl) & merger" -> "\\b:B . (b? fifo + dupl) & merger",
    "\\b:B . (b? fifo + lossy*lossy) ; merger" -> "\\b:B . (b? fifo + lossy*lossy) ; merger",
    "(\\x .drain^(x-1)) 3" -> "(\\x .drain^(x-1)) 3",
    "(\\x. lossy^x |x>4) ; ..." -> "(\\x. lossy^x |x>4) ; (\\n. merger^n | n>2 & n<6)",
    ".. ; merger!" -> "writer^8 ; merger! ; merger! ; reader!",
    "x;y{x=..,y=..}" -> "x ; y {x = lossy * fifo , y = merger}",
    "exrouter=.."->"writer ; dupl ; dupl*id ; (lossy*lossy ; dupl*dupl ; id*swap*id ; id*id*merger)*id ; id*id*drain ; reader^2",
    "zip=.."-> """zip 3
{
zip =
  \n.Tr((2*n)*(n-1))
    ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
     sym((2*n)*(n-1),2*n))
}""",
    "unzip=.." -> """unzip 3
{
unzip =
 \n.Tr((2*n)*(n-1))
   (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
    sym((2*n)*(n-1),2*n))
}""",
    "sequencer=.."-> """writer^3 ; sequencer 3 ; reader^3
                       |
                       |{
                       |zip =
                       |  \n.Tr((2*n)*(n-1))
                       |  ((id^(n-x)*sym(1,1)^x*id^(n-x))^x<--n;
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |unzip =
                       |  \n.Tr((2*n)*(n-1))
                       |  (((id^(x+1)*sym(1,1)^((n-x)-1)*id^(x+1))^x<--n);
                       |   sym((2*n)*(n-1),2*n)),
                       |
                       |sequencer =
                       |  \n.((dupl^n;unzip(n:I)) *
                       |    Tr(n)(sym(n-1,1);((fifofull;dupl)*((fifo ; dupl)^(n-1)));
                       |         unzip(n:I))) ;
                       |    (id^n*(zip(n:I) ; drain^n))
                       |}""".stripMargin,
  "nexrouters = ..." -> """writer ; nexrouter(3) ; reader!
                          |{
                          |  unzip =
                          |    \n.Tr((2*n)*(n - 1))
                          |    (((((id^(x+1))*(sym(1,1)^((n-x)-1)))*(id^(x+1)))^(x<--n));
                          |     sym((2*n)*(n-1),2*n))
                          |  ,
                          |  dupls =
                          |    \n.Tr(n-1)(id*(dupl^(n-1)) ; sym(1,(n-1)*2))
                          |  ,
                          |  mergers =
                          |    \n.Tr(n-1)(sym((n-1)*2,1) ; (id*(merger^(n-1))))
                          |  ,
                          |  nexrouter =
                          |    \n. (
                          |      (dupls(n+1)) ;
                          |      ((((lossy ; dupl)^n) ; (unzip(n)))*id) ;
                          |      ((id^n)*(mergers(n))*id) ;
                          |      ((id^n)*drain))
                          |}
                          |""".stripMargin
  )


  @JSExportTopLevel("reolive.WebReo.main")
  def main(content: html.Div) = {

//    // add header
//    d3.select(content).append("div")
//      .attr("id", "header")
//      .append("h1").text("Reo Live - Connector Families")

    val contentDiv = d3.select(content).append("div")
      .attr("id", "content")

    val rowDiv = contentDiv.append("div")
      .attr("class", "row")

    val colDiv1 = rowDiv.append("div")
      .attr("class", "col-sm-3")

    // add InputArea
    val inputDiv = panelBox(colDiv1,"Input (Shift-Enter to update)").append("div")
      .attr("id", "textBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("class","my-textarea")
      .attr("rows", "10")
      .attr("style", "width: 100%")
      .attr("placeholder", "dupl  ;  fifo * lossy")

    val outputBox = panelBox(colDiv1,"Type and instance").append("div")
      .attr("id", "outputBox")

    val buttonsDiv = panelBox(colDiv1,"examples").append("div")
      .attr("id", "buttons")
      .attr("style","padding: 2pt;")

    buttonsDiv
      .style("display:block; padding:2pt")


    val svgDiv = rowDiv.append("div")
      .attr("class", "col-sm-9")

    val svg = appendSvg(panelBox(svgDiv,"Circuit of the instance"),width,height)

    val mcrl2Box = panelBox(svgDiv,"mCRL2 of the instance",visible = false).append("div")
      .attr("id", "mcrl2Box")
    //      .style("margin-top", "4px")
    //      .style("border", "1px solid black")

    fgenerate("dupl  ;  fifo * lossy",outputBox,svg)

    /**
    Will evaluate the expression being written in the input box
      */
//      inputBox.onkeyup = (e: dom.Event) => {
//        fgenerate(inputBox.value,outputBox,canvasDiv)
//      }

    val inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {(e: dom.KeyboardEvent) =>
      if(e.keyCode.toInt == 13 && e.shiftKey) fgenerate(inputAreaDom.value,outputBox,svg)
      else ()
    }

    //inputArea.on("keyup", {(e: EventTarget, a: Int, b:UndefOr[Int]) =>println(e);fgenerate(inputAreaDom.value,outputBox)} : inputArea.DatumFunction[Unit])



    for (ops <- buttons ) yield genButton(ops,buttonsDiv, inputArea,outputBox, inputAreaDom,svg)

  }


  /**
    * Creates a collapsable pannel
    * */
  private def panelBox(parent:Block
                       ,title:String
                       ,visible:Boolean=true
                       ,copy: Boolean= false) : Block = {
    val wrap = parent.append("div").attr("class","panel-group")
      .append("div").attr("class","panel panel-default")
    if(!copy) {
      val header = wrap
        .append("div").attr("class", "panel-heading my-panel-heading")
        .append("h4").attr("class", "panel-title")
        .append("a").attr("data-toggle", "collapse")
        .attr("href", "#collapse-1" + title.hashCode)
        .attr("aria-expanded", visible.toString)
        .text(title)
    }
    else{
      val header = wrap
        .append("div").attr("class", "panel-heading my-panel-heading")
        .append("div").attr("class", "row").attr("style","padding-left: 0px")

      header
        .append("div").attr("class", "col-sm-10")
        .append("h4").attr("class", "panel-title")
        .append("a").attr("data-toggle", "collapse")
        .attr("href", "#collapse-1" + title.hashCode)
        .attr("aria-expanded", visible.toString)
        .text(title)

      header
        .append("div").attr("class", "col-sm-1")
        .append("button").attr("class", "btn btn-link btn-xs").attr("style", "height:18px")
          .text("Copy")
        .on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
        copyFunction
      }})
    }
    wrap
      .append("div").attr("id","collapse-1"+title.hashCode)
      .attr("class",if (visible) "panel-collapse collapse in" else "panel-collapse collapse")
      .attr("style",if (visible) "" else "height: 0px;")
      .attr("aria-expanded",visible.toString)
      .append("div").attr("class","panel-body my-panel-body")
  }

  private def copyFunction: Unit = {
    println("useless so far")
  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def fgenerate(input:String,outputInfo:Block,svg:Block): Unit={
    // clear output

    outputInfo.text("")

    // update output and run script
    DSL.parseWithError(input) match {
      case preo.lang.Parser.Success(result,_) =>
        try {
          val (typ,rest) = DSL.unsafeTypeOf(result)
          outputInfo.append("p")
            .text("[ "+Show(typ)+" ]")
          if (rest != BVal(true))
            outputInfo.append("p")
            .text(s"[  WARNING - did not check if ${Show(rest)} ]")
          Eval.unsafeInstantiate(result) match {
            case Some(reduc) =>
              // GOT A TYPE
              outputInfo.append("p")
                .text(Show(reduc)+":\n  "+
                  Show(DSL.unsafeTypeOf(reduc)._1))
              //println(Graph.toString(Graph(Eval.unsafeReduce(reduc))))

              val ccon = Eval.unsafeReduce(reduc)
              val graph = Graph(ccon)
              val size = graph.nodes.size
              val factor = Math.sqrt(size*10000/(density*9*6))
              width =  (9*factor).toInt
              height = (6*factor).toInt
              svg.attr("viewBox",s"00 00 $width $height")
              scalajs.js.eval(GraphsToJS(graph))
              d3.select("#mcrl2Box").html(Mcrl2Program(ccon).webString)
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
      case preo.lang.Parser.Failure(msg,_) => outputInfo.append("p").text("Parser failure: " + msg)
      case preo.lang.Parser.Error(msg,_) => outputInfo.append("p").text("Parser error: " + msg)
    }


  }


  private def genButton(ss:(String,String),buttonsDiv:Block, inputBox:Block,outputInfo:Block, inputAreaDom: html.TextArea,svg:Block): Unit = {
    val button = buttonsDiv.append("button")
        .text(ss._1)

    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
      inputAreaDom.value = ss._2
      fgenerate(ss._2,outputInfo,svg)
    }} : button.DatumFunction[Unit])

  }


  private def appendSvg(div: Block,width: Int, height: Int): Block = {
    val svg = div.append("svg")
//      .attr("width", "900")
//      .attr("height", "600")
//      .style("border", "black")
//      .style("border-width", "thin")
//      .style("border-style", "solid")
      .attr("style","margin: auto;")
      .attr("viewBox",s"0 0 $width $height")
      .attr("preserveAspectRatio","xMinYMin meet")
      .attr("id","svg-diagram")
      .style("margin", "auto")

    svg.append("g")
      .attr("class", "links")

    svg.append("g")
      .attr("class", "nodes")

    svg.append("g")
      .attr("class", "labels")

    svg.append("g")
      .attr("class", "paths")

    //inserting regular arrow at the end
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowout")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",20.5)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",7)
      .attr("markerHeight",7)
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
      .attr("refX",20.5)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",7)
      .attr("markerHeight",7)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 10,-5 L 0 ,0 L 10,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowout")
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-15)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",10)
      .attr("markerHeight",10)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L -10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowin")
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-22)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",10)
      .attr("markerHeight",10)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M -10,-5 L 0 ,0 L -10,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","boxmarker")
      .attr("viewBox","0 0 60 30")
      .attr("refX","30")
      .attr("refY","15")
      .attr("markerUnits","strokeWidth")
      .attr("markerWidth","18")
      .attr("markerHeight","9")
      .attr("stroke","black")
      .attr("stroke-width","6")
      .attr("fill","white")
      .attr("orient","auto")
      .append("rect")
        .attr("x","0")
        .attr("y","0")
        .attr("width","60")
        .attr("height","30")

    svg.append("defs")
      .append("marker")
      .attr("id","boxfullmarker")
      .attr("viewBox","0 0 60 30")
      .attr("refX","30")
      .attr("refY","15")
      .attr("markerUnits","strokeWidth")
      .attr("markerWidth","18")
      .attr("markerHeight","9")
      .attr("stroke","black")
      .attr("stroke-width","6")
      .attr("fill","black")
      .attr("orient","auto")
      .append("rect")
      .attr("x","0")
      .attr("y","0")
      .attr("width","60")
      .attr("height","30")

    svg
  }

}

