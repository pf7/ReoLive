package common.widgets

import common.backend.CCToNIFTA
import common.frontend.{AutomataToJS, IFTAToJS}
import preo.ast.CoreConnector
import ifta.{DSL, IFTA}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.backend.{Automata, PortAutomata}


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
      List(Left("all ports")-> (()=> draw(false)),
      Left("interface ports") -> (()=> draw(true)))),name="iftaAutomata")
    dom.document.getElementById("IFTA automaton of the instance")
      .firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if (!isVisible) drawAutomata() else deleteAutomaton()}

  }

  private def draw(hideInternal: Boolean): Unit = {
    if(isVisible){
      deleteAutomaton()
      drawAutomata(hideInternal)
    } else deleteAutomaton()
  }

  private def drawAutomata(hideInternal:Boolean=true): Unit = {
    try{
      // drawing ifta
      ifta = CCToNIFTA(dependency.get,hideInternal)
      val iftaSize = ifta.locs.size
      val iftaFactor = Math.sqrt(iftaSize*10000 / (densityAut * widthAutRatio * heightAutRatio))
      val width = (widthAutRatio * iftaFactor).toInt
      val height = (heightAutRatio * iftaFactor).toInt
      // evaluate js that generates the automaton
      box.attr("viewBox", s"00 00 $width $height")
//      dom.document.getElementById("iftaAutomata").innerHTML = DSL.toDot(ifta)
      scalajs.js.eval(IFTAToJS(ifta))
    }
    catch Box.checkExceptions(errorBox)
  }

  override def update(): Unit = if(isVisible) drawAutomata()

  private def deleteAutomaton(): Unit = {
    box.selectAll("g").html("")
  }


}