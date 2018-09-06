package common.widgets

import org.scalajs.dom
import org.scalajs.dom.{EventTarget, html}

import scala.scalajs.js.UndefOr

//todo: f must execute this.update
//todo: improve param function type
class InputBox(reload: => Unit)
  extends PanelBox[String]("Input (Shift-Enter to update)", None){

  var input: String = "dupl  ;  fifo * lossy"

  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = super.panelBox(div,visible /*, 80*/).append("div")
      .attr("id", "textBox")


    val button = wrap.select("table").append("th")
      .attr("float", "right")
      .attr("width", "20%")
      .append("button").attr("class", "btn btn-default btn-sm").attr("float", "right")

    button.append("span").attr("class", "glyphicon glyphicon-refresh")

    button.on("click", {(e: EventTarget, a: Int, b:UndefOr[Int])=> { reload }})

    val inputArea = inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("class","my-textarea")
      .attr("rows", "10")
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
      .attr("placeholder", input)

    inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload}
      else ()
    }
  }

  //todo: this function can be centralized. maybe....
  override def update: Unit = {
    val inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]
    if(input != "dupl  ;  fifo * lossy" || inputAreaDom.value != "")
      input = inputAreaDom.value
  }
}
