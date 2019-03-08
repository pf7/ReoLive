package common.widgets.virtuoso

import common.widgets.{Box, CodeBox, OutputArea}
import hub.analyse.ContextSwitch
import hub.{DSL, HubAutomata}
import preo.ast.CoreConnector
import preo.backend.Automata


/**
  * Created by guillecledou on 04/03/2019
  */


class VirtuosoCSInputBox(reloadCsInfo: => Unit)
  extends Box[String]("CS Analysis (Experimental)", List()) with CodeBox {

  override protected var input: String = ""
  override protected val boxId: String = "csInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the pattern formula (shift-enter)")
    )

  override protected val codemirror: String = "cs"

  override def reload(): Unit = {
    update()
    reloadCsInfo
  }

  def clear() =
    setValue("")

}
