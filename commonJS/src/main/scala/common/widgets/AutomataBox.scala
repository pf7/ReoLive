package common.widgets

import common.frontend.AutomataToJS
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Network.Mirrors
import preo.backend.{Automata, Circuit, PortAutomata}
import preo.common.TimeoutException


class AutomataBox(dependency: Box[CoreConnector], errorBox: OutputArea)
    extends Box[Automata]("Automaton of the instance", List(dependency)) {
  private var svg: Block = _
  private var automaton: Automata = _


  private val widthAutRatio = 7
  private val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: Automata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    svg= GraphBox.appendSvg(panelBox(div, visible),"automata")
    dom.document.getElementById("Automaton of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}

  }

  override def update(): Unit = if(isVisible) drawAutomata()


  private def drawAutomata(): Unit =
  try{
    // redundancy needed to generate new port names, so these can be linked to the graph being depicted.
//    val automaton = Automata.toAutWithRedundandy[PortAutomata](dependency.get)
    val mirrors = new Mirrors()
    //println("- Starting Automata drawing - 1st the circuit")
    Circuit(dependency.get,true,mirrors) // just to update mirrors
    //println("- Mirrors after circuit creation: "+mirrors)
    val automaton = Automata[PortAutomata](dependency.get,mirrors)
    //println("- Mirrors after Automata: "+mirrors)
    val sizeAut = automaton.getStates.size
    //              println("########")
    //              println(aut)
    //              println("++++++++")
    val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
    val width = (widthAutRatio * factorAut).toInt
    val height = (heightAutRatio * factorAut).toInt
    svg.attr("viewBox", s"00 00 $width $height")

    scalajs.js.eval(AutomataToJS(automaton,mirrors,"automata"))
  }
  catch Box.checkExceptions(errorBox)

  private def deleteAutomaton(): Unit = {
      svg.selectAll("g").html("")
    }
}
