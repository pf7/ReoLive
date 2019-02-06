package common.widgets.virtuoso

import common.widgets.{Box, OutputArea}
import hub.{HubAutomata, Var}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillecledou on 01/02/2019
  */


class VirtuosoInfoBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends Box[HubAutomata]("Hub Automaton Analysis", List(dependency)){
  private var aut: HubAutomata = _
  private var box: Block = _

  override def get: HubAutomata = aut

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible).append("div")
      .attr("id", "analysisBox")
    dom.document.getElementById("Hub Automaton Analysis").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) showInfo() else deleteInfo()}
  }

  override def update(): Unit = if(isVisible) showInfo()


  private def showInfo():Unit = try {
    if(box!=null) box.text("")
    aut = Automata[HubAutomata](dependency.get).serialize.simplify
    //    aut = dependency.get.asInstanceOf[HubAutomata]
    val (states,vars) = aut.memory
    var varsByType = vars.groupBy(_._1)
    if (box!=null) {
      box.append("p")
        .text("Memory:")
      box.append("p")
        .text(s"- ${states} states: ${Math.log(states) / Math.log(2)} bits \n")
      box.append("p")
        .text(if (vars.nonEmpty) {
          varsByType.map(v => s"- ${v._2.size} variables of type ${v._1}: ${v._2.size} * ${v._2.head._2 } bits").mkString("\n")
        }
        else "- 0 variables: 0 bits")
      box.append("p")
        .text("# of Transitions: " + aut.getTrans.size)
    }
  }
  catch Box.checkExceptions(errorBox)

  private def deleteInfo():Unit =
    box.text("")
}
