package common.widgets.Ifta

import common.backend.CCToFamily
import common.frontend.IFTAToJS
import common.widgets.{Box, GraphBox, OutputArea}
import ifta.IFTA
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector


/**
  * Receives information of an instance connector
  * @param dependency information of the instance connector
  * @param errorBox
  */
class IFTABox(dependency:Box[CoreConnector], errorBox:OutputArea)
    extends Box[IFTA]("IFTA automaton of the instance",List(dependency)){

  private var box: Block = _
  private var ifta: IFTA = _

  private val widthAutRatio = 7
  private val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: IFTA = ifta

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = GraphBox.appendSvg(panelBox(div,visible,buttons=
      List(Left("all ports")      -> (()=> draw(false,false),"Show internal and interface ports"),
          Left("interface ports") -> (()=> draw(false,true),"Show interface ports only"),
          Left("full names")       -> (()=> draw(true,false),"Identify all port names by connector index"))),name="iftaAutomata")
    dom.document.getElementById("IFTA automaton of the instance")
      .firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if (!isVisible) drawAutomata() else deleteAutomaton()}

  }

  private def draw(allNames:Boolean, hideInternal: Boolean): Unit = {
    if(isVisible){
//      deleteAutomaton()
      drawAutomata(allNames,hideInternal)
    } else deleteAutomaton()
  }

  private def drawAutomata(allNames:Boolean = false, hideInternal:Boolean=true): Unit = {
    deleteAutomaton()
    try{
      // drawing ifta
      ifta = CCToFamily.toIFTA(dependency.get,allNames,hideInternal)
      val iftaSize = ifta.locs.size
      val iftaFactor = Math.sqrt(iftaSize*10000 / (densityAut * widthAutRatio * heightAutRatio))
      val width = (widthAutRatio * iftaFactor).toInt
      val height = (heightAutRatio * iftaFactor).toInt
      // evaluate js that generates the automaton
      box.attr("viewBox", s"00 00 $width $height")
      scalajs.js.eval(IFTAToJS(ifta))
    }
    catch Box.checkExceptions(errorBox)
  }

  override def update(): Unit = if(isVisible) drawAutomata()

  private def deleteAutomaton(): Unit = {
    box.selectAll("g").html("")
  }


  def showFs(fs:Set[String]):Unit = {
    if (isVisible) {
      val sol = ifta.feats.map(f => if (fs contains f) f -> true else f -> false).toMap

      val edgeShow: Set[(Int, Int)] = ifta.edges.map(e =>
        if (e.fe.check(sol)) (e.act, e.fe).hashCode() -> 1
        else (e.act, e.fe).hashCode() -> 0)

      val showEdges = edgeShow.map(e => s"""["${e._1}", "${e._2}"]""").mkString("[", ",", "]")
      val updateEdges =
        s"""
           |var showEdges = new Map(${showEdges});
           |d3.select(".linksiftaAutomata")
           |  .selectAll("polyline")
           |  .style("stroke",function(d) {
           |      return (showEdges.get(d.id) == 1 ) ? "black" : "#cccccc"
           |    })
           |  .attr("marker-end", function(d) {
           |    return (showEdges.get(d.id) == 1) ? "url(#" + d.end + ")" : "url(#" + d.end + "light)"
           |  });
       """.stripMargin
      scalajs.js.eval(updateEdges)
    }
  }
}