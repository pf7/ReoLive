package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.frontend.mcrl2._
import preo.lang.FormulaParser

import scala.collection.mutable

class LogicBox(connector: Box[CoreConnector], outputBox: OutputArea)
  extends Box[String]("Modal Logic", List(connector)) {

  protected val default = "<all*> <fifo> true"

  protected var input: String = default

  protected var code: scalajs.js.Dynamic = _
  protected val boxId = "modalInputArea"

  //  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def update(): Unit = {
//    val inputAreaDom = dom.document.getElementById(boxId).asInstanceOf[html.TextArea]
//    if (input != "<true>[fifo] false" || inputAreaDom.value != "")
//      input = inputAreaDom.value
    val x = code.getValue()
    if (x != null) input = x.toString
  }

  def setValue(str: String): Unit = {
//    val inputAreaDom = dom.document.getElementById(boxId).asInstanceOf[html.TextArea]
//    inputAreaDom.value = str
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

//    inputDiv.append("textarea")
//      .attr("id", boxId)
//      .attr("class", "my-textarea")
//      .attr("rows", "3")
//      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
//      .attr("placeholder", input)
//
//    val inputAreaDom = dom.document.getElementById(boxId).asInstanceOf[html.TextArea]
//
//    inputAreaDom.onkeydown = { e: dom.KeyboardEvent =>
//      if (e.keyCode == 13 && e.shiftKey) {
//        e.preventDefault(); reload()
//      }
//      else ()
//    }
    inputDiv.append("textarea")
      .attr("id", boxId)
      .attr("name", boxId)
      .attr("class","my-textarea prettyprint lang-java")
//      .attr("rows", rows.toString)
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")

    buildCodeArea(default)
    //    val x = code.getValue()
    //    println(s"## got $x : ${x.getClass}")

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
    val model = Model(connector.get)
//    val newForm = LogicBox.expandFormula(input,model)
    val newForm = FormulaParser.parse(input) match {
      case FormulaParser.Success(result, next) =>
        try result.toString + "\n\n" + LogicBox.formula2mCRL2(result,model.getMultiActionsMap,outputBox)
        catch {
          case e:Throwable => {outputBox.error(e.getMessage); input}
        }
      case f: FormulaParser.NoSuccess => f.toString
    }
    outputBox.warning(newForm)
  }
}

object LogicBox {

//  /**
//    * Replaces all names of known containers
//    * @param input String with the logical formula
//    * @param model mCRL2 model to extract actions containing known containers
//    * @return New logical formula with container names replaced by regular expressions
//    */
//  @deprecated("Old primitive style to adapt mCRL2 formulas","")
//  def expandFormula(input:String, model: Model): String = {
//    val names = model.getMultiActionsMap
//
//    val input1 = "%.*\n".r.replaceAllIn(input,"\n")
//    val input2 = "/".r.replaceAllIn(input1,"_")
//    val res = "[a-z][a-zA-Z_0-9]*( *[|] *[a-z][a-zA-Z_0-9]*)*".r
//               .replaceAllIn(input2,x => getMNames(x.toString,names))
//    res
//  }

  def formula2mCRL2(f:Formula,names: mutable.Map[String, Set[Set[Action]]],
                    outbox:OutputArea): String =
    Formula.formula2mCRL2(f,list => getMNames(list.map(_.name).reverse.mkString("_"),names,outbox))


  /**
    * replace a given container name by a "or" of channel names
    * in the mCRL2 spec.
    * @param str name of the container
    * @param names mapping from container names to channels where they are used
    * @return "Or" list of the associated channels
//    */
  private def getMNames(str: String, names: mutable.Map[String, Set[Set[Action]]],
                        outbox: OutputArea): String = {
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
          if (a=="id") Some("sync")
//          println(s"## left res - ${res.mkString(".")}")
      }
    }
    res match {
      case None =>
        outbox.error(s"unknown container: ${actions.mkString("/")}")
        str //s"##${str}/${actions.mkString("/")}##"
      case Some(set) =>
        if (set.isEmpty) "false"
        else set.map(_.mkString("|")).mkString("("," || ",")")
    }
  }

//  private def getNames(str: String,names:mutable.Map[String,Set[Set[Action]]]): String = {
//    names.get(str) match {
//      case Some(acts) => acts.map(_.mkString("|")).mkString("("," + ",")")
//      case None => str
//    }
//  }
}
