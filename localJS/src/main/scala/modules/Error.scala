package modules

import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Selection

class Error {
  var errorArea: Selection[EventTarget] = _

  def spawn(block: Selection[EventTarget]): Unit = {
    errorArea = block.append("Div")
  }

  def error(msg:String): Unit = {
    val err = errorArea.append("div").attr("class", "alert alert-danger")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }

  def warning(msg:String): Unit ={
    val err = errorArea.append("div").attr("class", "alert alert-warning")
    for(s <- msg.split('\n')) err.append("p").attr("style","margin-top: 0px;").text(s)
  }

}
