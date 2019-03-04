package common.widgets.virtuoso

import common.widgets.{Box, CodeBox, OutputArea}
import hub.analyse.ContextSwitch
import hub.{DSL, HubAutomata}
import preo.ast.CoreConnector
import preo.backend.Automata


/**
  * Created by guillecledou on 04/03/2019
  */


class VirtuosoCSBox(connector: Box[CoreConnector], default: String, outputBox: OutputArea)
  extends Box[String]("CS Analysis (Experimental)", List(connector)) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "csInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the pattern formula (shift-enter)")
    )

  override protected val codemirror: String = "cs"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    // parse input
   try {
     var pattern = DSL.parsePattern(input)
     outputBox.message(pattern.toString()+"\n\n")
     var aut = Automata.fromOneToOneSimple[HubAutomata](connector.get).serialize.simplify
     val (found,trace,cs) = ContextSwitch(aut,pattern)
     outputBox.message(s"""
          |Found: ${found} \n
          |Trace: \n ${trace.mkString("- ","\n - ","")}
          |#ContextSwithces: ${cs}
        """.stripMargin)
   } catch Box.checkExceptions(outputBox)
  }

}
