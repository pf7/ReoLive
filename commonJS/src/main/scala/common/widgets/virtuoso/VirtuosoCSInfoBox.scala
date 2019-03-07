package common.widgets.virtuoso

import common.widgets.{Box, OutputArea}
import hub.analyse.ContextSwitch
import hub.{DSL, Guard, HubAutomata, Update}
import preo.ast.CoreConnector
import preo.backend.{Automata, ReoGraph}

/**
  * Created by guillecledou on 07/03/2019
  */


class VirtuosoCSInfoBox(dependency: Box[String], connector:Box[CoreConnector], errorBox: OutputArea)
  extends Box[String]("Context Switches Information", List(dependency)){

  var pattern: String = _
  var csInfo: Block = _

  override def get: String = pattern

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    csInfo = panelBox(div, visible).append("div")
      .attr("id", "csInfoBox")
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
      if (csInfo != null) {
        clear()
        try {
          var pattern = DSL.parsePattern(dependency.get)
          var aut = Automata.fromOneToOneSimple[HubAutomata](connector.get).serialize.simplify
          val (found,trace,cs) = ContextSwitch(aut,pattern)
          if (!found)
            errorBox.message(s"""Pattern not found""".stripMargin)
          else {
            // CS
            csInfo.append("p")
              .append("strong")
              .text(s"Context Switches: ${cs} (minimum)")

            csInfo.append("ul")
              .attr("style", "margin-bottom: 20pt;")
              .append("li")
                .text(s"${trace.size} transition(s) involved \n")

            // Pattern used
            csInfo.append("p")
              .append("strong")
              .text(s"Pattern used:")

            csInfo.append("ul")
              .attr("style", "margin-bottom: 20pt;")
              .append("li")
                .text(s"${pattern.mkString(",")} \n")
          }
      } catch Box.checkExceptions(errorBox)
    }
  }

  def clear():Unit = {
    errorBox.clear()
    csInfo.text("")
  }
}
