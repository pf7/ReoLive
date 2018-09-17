package common.widgets

import org.scalajs.dom
import org.singlespaced.d3js.Selection

class ErrorArea(id:String=""){
  type Block = Selection[dom.EventTarget]

  var errors: Block = _

  def init(div: Block): Unit = errors = div.append("div")

  def error(msg:String): Unit = {
    val err = errors.append("div").attr("class", "alert alert-danger")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }
  def warning(msg:String): Unit ={
    val err = errors.append("div").attr("class", "alert alert-warning")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }

  def clear(): Unit = errors.text("")
}
