package common.widgets

import org.scalajs.dom
import org.scalajs.dom.html

//todo: f must execute this.update
//todo: improve param function type
class InputBox(reload:() => Unit)
  extends PanelBox[String]("Input (Shift-Enter to update)", None){

  var input: String = "dupl  ;  fifo * lossy"

  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def init(div: Block): Unit = {
    val inputDiv = super.panelBox(div,true).append("div")
      .attr("id", "textBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("class","my-textarea")
      .attr("rows", "10")
      .attr("style", "width: 100%")
      .attr("placeholder", input)

    inputAreaDom = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {(e: dom.KeyboardEvent) =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload()}
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
