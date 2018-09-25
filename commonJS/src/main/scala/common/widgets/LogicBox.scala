package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.frontend.mcrl2.{Action, Model}

import scala.collection.mutable

class LogicBox(connector: Box[CoreConnector], outputBox: ErrorArea)
  extends Box[String]("Modal Logic", List(connector)) {

  var input: String = "<true>[fifo] false"

  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def update(): Unit = {
    val inputAreaDom = dom.document.getElementById("modalInputArea").asInstanceOf[html.TextArea]
    if (input != "<true>[fifo] false" || inputAreaDom.value != "")
      input = inputAreaDom.value
  }

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = super.panelBox(div, visible /*List("padding-right"->"25pt")*/
      /*, 80*/
      , buttons = List(
        Right("glyphicon glyphicon-refresh") -> (() => reload),
        Left("MA") -> (() => debugNames)
      ))
      .append("div")
      .attr("id", "modalBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "modalInputArea")
      .attr("class", "my-textarea")
      .attr("rows", "3")
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
      .attr("placeholder", input)

    inputAreaDom = dom.document.getElementById("modalInputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = { e: dom.KeyboardEvent =>
      if (e.keyCode == 13 && e.shiftKey) {
        e.preventDefault(); reload()
      }
      else ()
    }
  }

  private def debugNames(): Unit = {
    outputBox.clear()
    if (connector.get == null)
      outputBox.warning("null model...")
    else {
      val model = Model(connector.get)
      outputBox.warning(model.getMultiActionsMap
        .map(kv => "'" + kv._1 + "'" + ":" + kv._2.map("\n - " + _).mkString(""))
        .mkString("\n"))
    }
  }

  private def reload(): Unit = {
    update()
    outputBox.clear()
    val model = Model(connector.get)
    val newForm = LogicBox.expandFormula(input,model)
    outputBox.warning(newForm)
  }
}

object LogicBox {
  def expandFormula(input:String, model: Model): String = {
    val names = model.getMultiActionsMap

    val res = "[a-z][a-zA-Z0-9]+( *[|] *[a-z][a-zA-Z0-9]+)*".r
               .replaceAllIn(input,x => getMNames(x.toString,names))
//    val res2 = "[a-z][a-zA-Z0-9]+".r
//               .replaceAllIn(res1,x => getNames(x.toString,names))
//    val res = input.replaceAll("([a-z][a-zA-Z0-9]+)", getNames("$1",names.toMap))
    res
  }

  private def getMNames(str: String, names: mutable.Map[String, Set[Set[Action]]]): String = {
    val actions = str.split(" *[|] *")
    var res: Option[Set[Set[Action]]] = None
//    println(s"starting: ${actions.mkString(".")}")
    for (a <- actions) {
      (names.get(a),res) match {
        case (Some(mas),Some(acc)) =>
          res = Some(acc intersect mas)
//          println(s"updated res - ${res.mkString(".")}")
        case (Some(mas),None) =>
          res = Some(mas)
//          println(s"reset res - ${res.mkString(".")}")
        case (None,_) =>
//          println(s"## left res - ${res.mkString(".")}")
      }
    }
    res match {
      case None => str //s"##${str}/${actions.mkString("/")}##"
      case Some(set) =>
        if (set.isEmpty) "false"
        else set.map(_.mkString("|")).mkString("("," + ",")")
    }
  }

//  private def getNames(str: String,names:mutable.Map[String,Set[Set[Action]]]): String = {
//    names.get(str) match {
//      case Some(acts) => acts.map(_.mkString("|")).mkString("("," + ",")")
//      case None => str
//    }
//  }
}
