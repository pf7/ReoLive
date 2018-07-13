package reolive



import D3Lib.CopytoClipboard._
import D3Lib.{AutomataToJS, GraphsToJS, JsonLoader}
import org.scalajs.dom
import dom.{EventTarget, MouseEvent, html}
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import org.singlespaced.d3js.{Selection, d3}
import preo.frontend.{Eval, Show, Simplify}
import preo.common.{GenerationException, TypeCheckException}
import preo.backend._
import preo.DSL
import preo.ast.BVal
import preo.frontend.mcrl2.Model
import preo.ast.CoreConnector

import scala.scalajs.js.{JavaScriptException, UndefOr}
import scalajs.js.annotation.JSExportTopLevel
import scalatags.JsDom.all._

import scala.util.parsing.json.JSONObject


/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo extends{

  type Block = Selection[dom.EventTarget]
  var width = 700
  var height = 400

  val widthCircRatio = 7
  val heightCircRatio = 3
  val densityCirc = 0.5 // nodes per 100x100 px

  val widthAutRatio = 7
  val heightAutRatio = 3
  val densityAut = 0.2 // nodes per 100x100 px

//  var connector: CoreConnector = null

  private var graph: Map[String, String] = null
  private var gsize: Int = 0
  private var automata: Map[String, String] = null
  private var asize: Int = 0
  private var mcrl2: String = ""

  private val buttons = Seq(
    "writer"->"writer", "reader"->"reader",
    "fifo"->"fifo",     "merger"->"merger",
    "dupl"->"dupl",     "drain"->"drain",
    "fifo*writer ; drain"->"fifo*writer ; drain",
    "\\x . fifo^x*writer ; drain^2" -> "\\x . fifo^x*writer ; drain^2",
    "(\\x.fifo^x) ; (\\n.drain^n)" -> "(\\x.fifo^x) ; (\\n.drain^n)",
    //    "\\b:B . (b? fifo + dupl) & merger" -> "\\b:B . (b? fifo + dupl) & merger",
    "\\b:B . (b? fifo + lossy*lossy) ; merger" -> "\\b:B . (b? fifo + lossy*lossy) ; merger",
    "(\\x .drain^(x-1)) 3" -> "(\\x .drain^(x-1)) 3",
    "(\\x. lossy^x |x>2) ; ..." -> "(\\x. lossy^x |x>2) ; (\\n. merger^n | n>1 & n<6)",
    ".. ; merger!" -> "writer^8 ; merger! ; merger! ; reader!",
    "x;y{x=..,y=..}" -> "x ; y {x = lossy * fifo , y = merger}",
    "exrouter=.."->"""dupl ; dupl*id ;
                     |(lossy;dupl)*(lossy;dupl)*id ;
                     |id*merger*id*id ;
                     |id*id*swap ;
                     |id*drain*id""".stripMargin,
    //    "exrouter=.."->"writer ; dupl ; dupl*id ; (lossy*lossy ; dupl*dupl ; id*swap*id ; id*id*merger)*id ; id*id*drain ; reader^2",
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
                       |fifoloop = \n. Tr(n)(
                       |   sym(n-1,1);
                       |  (fifofull; dupl) * (fifo; dupl)^(n-1);
                       |  unzip n ),
                       |
                       |sequencer =
                       |  \n.(dupl^n; unzip n) * fifoloop n ;
                       |    id^n * (zip n; drain^n)
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
                            |""".stripMargin,
    "Prelude"->
      """id
        |{
        |  writer    = writer,
        |  reader    = reader,
        |  fifo      = fifo,
        |  fifofull  = fifofull,
        |  drain     = drain,
        |  id        = id,
        |  dupl      = dupl,
        |  lossy     = lossy,
        |  merger    = merger,
        |  swap      = swap,
        |  exrouter  = exrouter,
        |  exrouters = exrouters,
        |  ids       = ids,
        |  node      = node,
        |  dupls     = dupls,
        |  mergers   = mergers,
        |  zip       = zip,
        |  unzip     = unzip,
        |  fifoloop  = fifoloop,
        |  sequencer = sequencer
        |}
      """.stripMargin
  )


  @JSExportTopLevel("reolive.RemoteReo.main")
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
      .attr("class", "col-sm-4")

    // add InputArea
    val inputDiv = panelBox(colDiv1,"Input (Shift-Enter to update)").append("div")
      .attr("id", "textBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("class","my-textarea")
      .attr("rows", "10")
      .attr("style", "width: 100%")
      .attr("placeholder", "dupl  ;  fifo * lossy")

    val errors = colDiv1.append("div")

    val typeBox = panelBox(colDiv1,"Type").append("div")
      .attr("id", "typeBox")

    val instanceBox = panelBox(colDiv1,"Concrete instance").append("div")
      .attr("id", "instanceBox")

    val buttonsDiv = panelBox(colDiv1,"examples").append("div")
      .attr("id", "buttons")
      .attr("style","padding: 2pt;")

    buttonsDiv
      .style("display:block; padding:2pt")


    val svgDiv = rowDiv.append("div")
      .attr("class", "col-sm-8")

    val svg = appendSvg(panelBox(svgDiv,"Circuit of the instance"),"circuit")

    val panelAut = panelBox(svgDiv,"Automaton of the instance (under development)",visible = false)
    val svgAut = appendSvg(panelAut,"automata")

    val mcrl2Box = panelBox(svgDiv,"mCRL2 of the instance",visible = false).append("div")
      .attr("id", "mcrl2Box")
    //      .style("margin-top", "4px")
    //      .style("border", "1px solid black")

    fgenerate("dupl  ;  fifo * lossy",typeBox,instanceBox,svg,svgAut,errors)

    /**
    Will evaluate the expression being written in the input box
      */
    //      inputBox.onkeyup = (e: dom.Event) => {
    //        fgenerate(inputBox.value,typeBox,canvasDiv)
    //      }

    val inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {(e: dom.KeyboardEvent) =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault() ; fgenerate(inputAreaDom.value,typeBox,instanceBox,svg,svgAut,errors)}
      else ()
    }

    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {(e: MouseEvent) =>
      if(!isVisible("Circuit of the instance")) drawConnector(graph, gsize, svg)
      else deleteDrawing(svg)
    }

    dom.document.getElementById("Automaton of the instance (under development)").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {(e: MouseEvent) =>
      if(!isVisible("Automaton of the instance (under development)")) drawAutomata(automata, asize,svgAut)
      else deleteDrawing(svgAut)
    }

    dom.document.getElementById("mCRL2 of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {(e: MouseEvent) =>
      if(!isVisible("mCRL2 of the instance")) produceMcrl2(mcrl2)
      else deleteModel()
    }

    //inputArea.on("keyup", {(e: EventTarget, a: Int, b:UndefOr[Int]) =>println(e);fgenerate(inputAreaDom.value,outputBox)} : inputArea.DatumFunction[Unit])




    for (ops <- buttons ) yield genButton(ops,buttonsDiv, inputArea,typeBox, instanceBox, inputAreaDom,svg,svgAut,errors)

  }


  /**
    * Creates a collapsable pannel
    * */
  private def panelBox(parent:Block
                       ,title:String
                       ,visible:Boolean=true
                       ,copy: Boolean= false) : Block = {
    def copyFunction(): Unit =
      println("useless so far")

    var expander: Block = parent
    val wrap = parent.append("div").attr("class","panel-group")
      .append("div").attr("class","panel panel-default").attr("id",title)
    if(!copy) {
      expander = wrap
        .append("div").attr("class", "panel-heading my-panel-heading")
        .append("h4").attr("class", "panel-title")
        .append("a").attr("data-toggle", "collapse")
        .attr("href", "#collapse-1" + title.hashCode)
        .attr("aria-expanded", visible.toString)
      if(!visible)
        expander.attr("class","collapsed")
      expander
        .text(title)

    }
    else{
      val header = wrap
        .append("div").attr("class", "panel-heading my-panel-heading")
        .append("div").attr("class", "row").attr("style","padding-left: 0px")

      expander = header
        .append("div").attr("class", "col-sm-10")
        .append("h4").attr("class", "panel-title")
        .append("a").attr("data-toggle", "collapse")
        .attr("href", "#collapse-1" + title.hashCode)
        .attr("aria-expanded", visible.toString)
        .attr("class","collapsed")
      expander
        .text(title)

      header
        .append("div").attr("class", "col-sm-1")
        .append("button").attr("class", "btn btn-link btn-xs").attr("style", "height:18px")
        .text("Copy")
        .on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
          copyFunction()
        }})
    }
    val res = wrap
      .append("div").attr("id","collapse-1"+title.hashCode)
      .attr("class",if (visible) "panel-collapse collapse in" else "panel-collapse collapse")
      .attr("style",if (visible) "" else "height: 0px;")
      .attr("aria-expanded",visible.toString)
      .append("div").attr("class","panel-body my-panel-body")

    res
  }

  private def isVisible(id:String): Boolean = {
    val es = dom.document.getElementsByClassName("collapsed")
    var foundId = false
    for (i <- 0 until es.length) {
      println(es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      //      println("### - "+es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      foundId = foundId || es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value == id
    }

    //    println("### - "+es.length)
    //    println("### - "+es.item(0).localName)
    //    println("### - "+es.item(0).parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)

    //    val res = expander.attr("aria-expander") == "true"
    //    println("--- "+expander.html().render)
    //    println("--- "+expander.classed("collapsed"))
    //    println("--- "+expander.attr("aria-expander"))
    //    println("$$$ "+ (!foundId))
    !foundId
  }




  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def fgenerate(input:String,typeInfo:Block,instanceInfo:Block,svg:Block,svgAut:Block,errors:Block): Unit= {
    // clear output

    val socket = new WebSocket("ws://localhost:9000/message")
    var message: String = null

    socket.onmessage = { (e: MessageEvent) => {process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors); socket.close()}}// process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors) }

    socket.addEventListener("open", (e: Event) => {
      socket.send(input)
    })

  }

  private def process(input: String, typeInfo: Block, instanceInfo: Block, svg:Block, svgAut:Block, errors:Block): Unit = {

    val result = JsonLoader(input)

//
//    val (typ, reducTyp, con, graph, automata, mcrl2) = JsonLoader(input)


    typeInfo.text("")
    instanceInfo.text("")
    errors.text("")
//    println(result)
    result match{
      case Right(message) => error(errors, message)
      case Left((typ,reducTyp, con, (gr, gs), (aut, as), mc)) => {
        typeInfo.append("p")
          .text(typ)
        instanceInfo.append("p")
          .text(Show(con)+":\n  "+
            reducTyp)
        this.graph = gr
        this.gsize = gs
        this.automata = aut
        this.asize = as
        this.mcrl2 = mc

//        println(aut)
//        println(as)
//
//        println(automata)
//        println(asize)

        if(isVisible("Circuit of the instance")) {
          drawConnector(graph, gsize, svg)
        }

        if (isVisible("Automaton of the instance (under development)")) {
          drawAutomata(automata, asize, svgAut)
        }

        if(isVisible("mCRL2 of the instance")) {
          produceMcrl2(mcrl2)
        }
      }
    }

  }

  private def deleteDrawing(svg: RemoteReo.Block): Unit = {
    svg.selectAll("g").html("")
  }

  private def deleteModel(): Unit = {
    d3.select("#mcrl2Box").html("")
  }

  private def drawConnector(graph: Map[String, String], size: Int, svg: RemoteReo.Block): Unit = {

    val factor = Math.sqrt(size*10000/(densityCirc*widthCircRatio*heightCircRatio))
    val width =  (widthCircRatio*factor).toInt
    val height = (heightCircRatio*factor).toInt
    svg.attr("viewBox",s"00 00 $width $height")
    scalajs.js.eval(GraphsToJS.remoteBuild(graph))
  }

  private def drawAutomata(aut: Map[String, String],sizeAut:Int, svgAut: RemoteReo.Block): Unit = {
    //              println("########")
    //              println(aut)
    //              println("++++++++")
    val factorAut = Math.sqrt(sizeAut * 3333 / (densityAut * widthAutRatio * heightAutRatio))
    val width = (widthAutRatio * factorAut).toInt
    val height = (heightAutRatio * factorAut).toInt
    svgAut.attr("viewBox", s"00 00 $width $height")

    scalajs.js.eval(AutomataToJS.remoteBuild(aut))
  }

  private def produceMcrl2(mcrl2: String): Unit = {
    d3.select("#mcrl2Box").html(mcrl2)
  }

  private def error(errors:Block,msg:String): Unit = {
    val err = errors.append("div").attr("class", "alert alert-danger")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }
  private def warning(errors:Block,msg:String): Unit ={
    val err = errors.append("div").attr("class", "alert alert-warning")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }


  private def genButton(ss:(String,String),buttonsDiv:Block, inputBox:Block,typeInfo:Block,
                        instanceInfo:Block,inputAreaDom: html.TextArea,svg:Block,svgAut:Block,errors:Block): Unit = {
    val button = buttonsDiv.append("button")
      .text(ss._1)

    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
      inputAreaDom.value = ss._2
      fgenerate(ss._2,typeInfo,instanceInfo,svg,svgAut,errors)
    }} : button.DatumFunction[Unit])

  }


  private def appendSvg(div: Block,name: String): Block = {
    val svg = div.append("svg")
      //      .attr("width", "900")
      //      .attr("height", "600")
      //      .style("border", "black")
      //      .style("border-width", "thin")
      //      .style("border-style", "solid")
      .attr("style","margin: auto;")
      .attr("viewBox",s"0 0 $width $height")
      .attr("preserveAspectRatio","xMinYMin meet")
      .attr("id",name)
      .style("margin", "auto")

    svg.append("g")
      .attr("class", "links"+name)

    svg.append("g")
      .attr("class", "nodes"+name)

    svg.append("g")
      .attr("class", "labels"+name)

    svg.append("g")
      .attr("class", "paths"+name)

    //inserting regular arrow at the end
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowout"+name)
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
      .attr("id","endarrowin"+name)
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
      .attr("id","startarrowout"+name)
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
      .attr("id","startarrowin"+name)
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
      .attr("id","boxmarker"+name)
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
      .attr("id","boxfullmarker"+name)
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

