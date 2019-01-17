package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.frontend.mcrl2._
import preo.lang.ParserUtils

class LogicBox(connector: Box[CoreConnector], default: String, outputBox: OutputArea)
  extends Box[String]("Modal Logic", List(connector)) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "modalInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
          Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the logical formula (shift-enter)"),
          Left("MA") -> (() => debugNames, "Map actions in the formula to sets of actions in the mCRL2 specification")
        )

  override protected val codemirror: String = "modal"

  override def reload(): Unit = {
    update()
    outputBox.clear()
//    val model = Model(connector.get)
////    val newForm = LogicBox.expandFormula(input,model)
//    val newForm = parseFormula(model,input)
    //    outputBox.warning(newForm)

    ParserUtils.parseFormula(input) match {
      case Left(err) => outputBox.error(err)
      case Right(form) =>
        val prefixes = Formula.notToHide(form).filterNot(_==Nil)
        if (prefixes.nonEmpty)
          outputBox.message(s"Exposing ${prefixes.map(_.map(_.name).mkString("[", "/", "]")).mkString(",")}")
        ParserUtils.hideBasedOnFormula(form,connector.get) match {
          case Left(err) => outputBox.error(err)
          case Right((_,formExpanded)) =>
            outputBox.message("Expanded formula:\n" + formExpanded)
      }
    }
  }

  protected def debugNames(): Unit = {
    outputBox.clear()
    if (connector.get == null)
      outputBox.warning("null model...")
    else {
      val model = Model(connector.get)
      outputBox.warning(model.getMultiActionsMap
        .map(kv => "'" + kv._1 + "'" + ":" + kv._2.map("\n - " + _.mkString(", ")).mkString(""))
        .mkString("\n"))
    }
  }


}
