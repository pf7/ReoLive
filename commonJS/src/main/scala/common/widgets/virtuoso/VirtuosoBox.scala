package common.widgets.virtuoso

import common.widgets.virtuoso.VirtuosoParser.Result
import common.widgets.{Box, CodeBox, OutputArea}
import preo.DSL
import preo.ast.{BVal, Connector, CoreConnector}
import preo.frontend.{Eval, Show, Simplify}

class VirtuosoBox(globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("Hub Composer", Nil) with CodeBox {

    override protected var input: String = default
    override protected val boxId: String = "virtuosoInputArea"
    override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
      List(
        Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the Hub (shift-enter)")
      )

    override protected val codemirror: String = "virtuoso"

    override def reload(): Unit = {
      update()
      outputBox.clear()
      globalReload
    }

}
