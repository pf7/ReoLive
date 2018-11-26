package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.frontend.mcrl2._
import preo.lang.ParserUtils

class LogicBox(connector: Box[CoreConnector], default: String, outputBox: OutputArea)
  extends Box[String]("Modal Logic", List(connector)) {

//  protected val default = "<all*> <fifo> true"

  protected var input: String = default

  protected var code: scalajs.js.Dynamic = _
  protected val boxId = "modalInputArea"

  //  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def update(): Unit = {
    val x = code.getValue()
    if (x != null) input = x.toString
  }

  def setValue(str: String): Unit = {
    code.setValue(str)
  }

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = panelBox(div, visible /*List("padding-right"->"25pt")*/
      /*, 80*/
      , buttons = List(
        Right("glyphicon glyphicon-refresh") -> (() => reload),
        Left("MA") -> (() => debugNames)
      ))
      .append("div")
      .attr("id", "modalBox")

    inputDiv.append("textarea")
      .attr("id", boxId)
      .attr("name", boxId)
      .attr("class","my-textarea prettyprint lang-java")
//      .attr("rows", rows.toString)
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")

    buildCodeArea(default)

    val realTxt = dom.document.getElementById("modalBox")
      .childNodes(1).childNodes(0).childNodes(0).asInstanceOf[html.TextArea]
    realTxt.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload()}
      else ()
    }

  }

  protected def buildCodeArea(txt: String) = {
    val codemirror = scalajs.js.Dynamic.global.CodeMirror
    val lit = scalajs.js.Dynamic.literal(
      lineNumbers = true, matchBrackets = true, theme = "neat", id="strangeID", mode="modal")
    code = codemirror.fromTextArea(dom.document.getElementById(boxId),lit)
    code.setValue(txt)
  }

  private def debugNames(): Unit = {
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

  private def reload(): Unit = {
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

}
