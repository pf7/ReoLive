package common.widgets

import org.scalajs.dom
import org.scalajs.dom.{EventTarget, html}

import scala.scalajs.js.UndefOr

//todo: f must execute this.update
//todo: improve param function type
/**
  * Box used to receive an input text, and to make this available to the other boxes as a String.
  * @param reload load the value from the box and update its internal state  containing the text.
  * @param default
  * @param id
  * @param rows
  */
class InputBox(reload: => Unit, default:String="", id:String="", rows:Int = 10,
               title: String,
               refreshLabel: String =  "Load the connector (shift-enter)")
  extends Box[String](title, Nil) with Setable[String]{

  var input: String = default
  private var inputAreaDom: html.TextArea = _

  val boxId = "inputArea_"+id

  override def get: String = input

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = super.panelBox(div,visible /*, 80*/ ,
      buttons=List(Right("glyphicon glyphicon-refresh")-> (()=>reload, refreshLabel)))
      .append("div")
      .attr("id", "textBox_"+id)


//    scalajs.js.eval(s"""var myCodeMirror = CodeMirror(document.body, {
//                       |  value: "function myScript(){return 100;}",
//                       |  mode:  "javascript"
//                       |});
//                       |""".stripMargin)

    val inputArea = inputDiv.append("textarea")
      .attr("id", boxId)
      .attr("name", boxId)
      .attr("class","my-textarea prettyprint lang-java")
      .attr("rows", rows.toString)
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
//      .attr("placeholder", input)
      .text(input)
    //    <textarea style="height: 50vh; display: none;" form="debuggerRunForm" name="debuggerCode" id="debuggerCode"></textarea>

    inputAreaDom = dom.document.getElementById("inputArea_"+id).asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload}
      else ()
    }
  }

  //todo: this function can be centralized. maybe....
  override def update: Unit = {
    val inputAreaDom = dom.document.getElementById("inputArea_"+id).asInstanceOf[html.TextArea]
    if(input != default || inputAreaDom.value != "")
      input = inputAreaDom.value
  }

  def setValue(str: String) = {
    inputAreaDom.value = str
  }
}
