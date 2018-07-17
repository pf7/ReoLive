package common.widgets

import common.frontend.AutomataToJS
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.{Automata, PortAutomata}

class AutomataBox(dependency: PanelBox[CoreConnector]) extends PanelBox[Automata]("Automaton of the instance (under development)", Some(dependency)) {
  private var svg: Block = _
  private var automaton: Automata = _


  private val widthAutRatio = 7
  private val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: Automata = automaton

  override def init(div: Block): Unit = {
    svg= PanelBox.appendSvg(panelBox(div, false),"automata")
    dom.document.getElementById("Automaton of the instance (under development)").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {(e: MouseEvent) => if(!isVisible) drawAutomata else deleteAutomaton}

  }

  override def update: Unit = if(isVisible) drawAutomata


  private def drawAutomata: Unit = {
    automaton = Automata[PortAutomata](dependency.get)
    val sizeAut = automaton.getStates.size
    //              println("########")
    //              println(aut)
    //              println("++++++++")
    val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
    val width = (widthAutRatio * factorAut).toInt
    val height = (heightAutRatio * factorAut).toInt
    svg.attr("viewBox", s"00 00 $width $height")

    scalajs.js.eval(AutomataToJS(automaton))
  }

  private def deleteAutomaton: Unit = {
      svg.selectAll("g").html("")
    }
}
