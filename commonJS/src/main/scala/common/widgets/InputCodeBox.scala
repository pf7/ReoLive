package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html

//todo: f must execute this.update
//todo: improve param function type
/**
  * Box used to receive an input text, and to make this available to the other boxes as a String.
  * @param reload load the value from the box and update its internal state  containing the text.
  * @param default
  * @param id
  * @param rows
  */
class InputCodeBox(reload: => Unit, default:String="", id:String="", rows:Int = 10)
  extends Box[String]("Input", Nil){

  var input: String = default
  private var inputAreaDom: html.TextArea = _

  private var code: scalajs.js.Dynamic = _
  val boxId = "inputArea_"+id

  override def get: String = input

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = super.panelBox(div,visible /*, 80*/ ,
      buttons=List(Right("glyphicon glyphicon-refresh")-> (()=>reload)))
      .append("div")
      .attr("id", "textBox_"+id)

    val inputArea = inputDiv.append("textarea")
      .attr("id", boxId)
      .attr("name", boxId)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", rows.toString)
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")

    buildCodeArea(default)
    val x = code.getValue()
    println(s"## got $x : ${x.getClass}")

  }

  //  private def getTextArea() = scalajs.js.eval(
  //    s"""CodeMirror.fromTextArea(document.getElementById("$boxId"), {
  //       |    lineNumbers: true,
  //       |    matchBrackets: true,
  //       |    theme: "monokai"
  //       |  });  """.stripMargin)

  private def buildCodeArea(txt: String) = {
    val codemirror = scalajs.js.Dynamic.global.CodeMirror
    val lit = scalajs.js.Dynamic.literal(lineNumbers = true, matchBrackets = true, theme = "neat")
    code = codemirror.fromTextArea(dom.document.getElementById(boxId),lit)
    code.setValue(txt)
  }

  def setValue(str: String) = {
    code.setValue(str)
  }


  override def update: Unit = {
    val x = code.getValue()
    if (x != null) input = x.toString
//    println(s"## got $x : ${x.getClass}")
  }
}
