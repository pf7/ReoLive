//package reolive
//
//import D3Lib.{AutomataToJS, GraphsToJS}
//import org.scalajs.dom
//import org.scalajs.dom.{EventTarget, html}
//import org.singlespaced.d3js.{Selection, d3}
//import preo.ast.CoreConnector
//import preo.backend.{Automata, Graph, PortAutomata}
//import preo.modelling.Mcrl2Model
//import scalatags.JsDom.all.s
//
//import scala.scalajs.js.UndefOr
//
//abstract class ReoSite {
//
//
//  val widthCircRatio = 7
//  val heightCircRatio = 3
//  val densityCirc = 0.5 // nodes per 100x100 px
//
//  val widthAutRatio = 7
//  val heightAutRatio = 3
//  val densityAut = 0.2 // nodes per 100x100 px
//
//
//
//
//
//
//
//
//
//
//  protected def drawConnector(svg: WebReo.Block): Unit = {
//    val graph = Graph(connector)
//    val size = graph.nodes.size
//    val factor = Math.sqrt(size*10000/(densityCirc*widthCircRatio*heightCircRatio))
//    val width =  (widthCircRatio*factor).toInt
//    val height = (heightCircRatio*factor).toInt
//    svg.attr("viewBox",s"00 00 $width $height")
//    scalajs.js.eval(GraphsToJS(graph))
//  }
//
//  protected def drawAutomata(svgAut: WebReo.Block): Unit = {
//    val aut = Automata[PortAutomata](connector)
//    val sizeAut = aut.getStates.size
//    //              println("########")
//    //              println(aut)
//    //              println("++++++++")
//    val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
//    val width = (widthAutRatio * factorAut).toInt
//    val height = (heightAutRatio * factorAut).toInt
//    svgAut.attr("viewBox", s"00 00 $width $height")
//
//    scalajs.js.eval(AutomataToJS(aut))
//  }
//
//  protected def produceMcrl2(): Unit = {
//    d3.select("#mcrl2Box").html(Mcrl2Model(connector).webString)
//  }
//
//  protected def error(errors:Block,msg:String): Unit = {
//    val err = errors.append("div").attr("class", "alert alert-danger")
//    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
//  }
//
//
//  protected def warning(errors:Block,msg:String): Unit ={
//    val err = errors.append("div").attr("class", "alert alert-warning")
//    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
//  }
//
//
//  protected def genButton(ss:(String,String),buttonsDiv:Block, inputBox:Block,typeInfo:Block,
//                        instanceInfo:Block,inputAreaDom: html.TextArea,svg:Block,svgAut:Block,errors:Block): Unit = {
//    val button = buttonsDiv.append("button")
//      .text(ss._1)
//
//    button.on("click",{(e: EventTarget, a: Int, b:UndefOr[Int])=> {
//      inputAreaDom.value = ss._2
//      fgenerate(ss._2,typeInfo,instanceInfo,svg,svgAut,errors)
//    }} : button.DatumFunction[Unit])
//
//  }
//}
