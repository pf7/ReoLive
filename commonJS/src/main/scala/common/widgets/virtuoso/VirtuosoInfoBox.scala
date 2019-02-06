package common.widgets.virtuoso

import common.widgets.{Box, OutputArea}

import hub.{HubAutomata, Var}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import hub.{HubAutomata, Ltrue, Var}
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

//<<<<<<< HEAD
  override def update(): Unit = if(isVisible) showInfo()


//  private def showInfo():Unit = try {
//    if(box!=null) box.text("")
//    aut = Automata[HubAutomata](dependency.get).serialize.simplify
//    //    aut = dependency.get.asInstanceOf[HubAutomata]
//    val (states,vars) = aut.memory
//    var varsByType = vars.groupBy(_._1)
//    if (box!=null) {
//      box.append("p")
//        .text("Memory:")
//      box.append("p")
//        .text(s"- ${states} states: ${Math.log(states) / Math.log(2)} bits \n")
//=======
  private def showInfo(): Unit = try {
    if(box!=null) {
      box.text("")
      aut = Automata[HubAutomata](dependency.get).serialize.simplify
      //    aut = dependency.get.asInstanceOf[HubAutomata]

      // Memory
      val (states, vars) = aut.memory
      var mem = 0
      var varsByType = vars.groupBy(_._1)
      box.append("p")
        .append("strong")
        .text("Memory")
      val list = box.append("ul")
      list.append("li")
        .text(s"${states} state(s): ${Math.log(states) / Math.log(2)} bit(s) \n")
      mem += (Math.log(states) / Math.log(2)).toInt
      list.append("li")
        .text(if (vars.nonEmpty) {
          varsByType.map(v => s"${v._2.size} variable(s) of type ${v._1}: ${v._2.size} * ${v._2.head._2} bit(s)").mkString("\n")
        }
        else "0 variables: 0 bits")
      varsByType.foreach(v =>  mem += v._2.size * v._2.head._2)
      list.append("li")
        .text(s"Total: $mem bit${if( mem != 1) "s" else ""} \n")

      // Transitions
      var loc = 0
      box.append("p")
        .append("strong")
        .text("Size")
      val list2 = box.append("ul")
      list2.append("li")
        .text(s"${aut.trans.size} transition(s), ${states} state(s), ${vars.size} variable(s) (1 loc each)")
      loc += aut.trans.size + states + vars.size
      var g = 0
      for (t <- aut.trans) if (t._2._3 != Ltrue) g += 1
      list2.append("li")
        .text(s"$g non-empty guards (1 loc each)")
      loc += g
      var ups = 0
      for (t <- aut.trans) ups += t._2._4.size
      list2.append("li")
        .text(s"$ups assignment instructions (1 loc each)")
      loc += ups
      list2.append("li")
        .text(s"Total: $loc loc${if(loc!=1)"s"} \n")

    }
  }
  catch Box.checkExceptions(errorBox)

  private def deleteInfo():Unit =
    box.text("")
}
