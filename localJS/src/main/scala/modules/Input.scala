package modules

import common.ParserException
import org.scalajs.dom
import org.scalajs.dom.{EventTarget, html}
import org.singlespaced.d3js.Selection
import preo.DSL
import reolive.Structure
import sourcecode.Text.generate

class Input extends Module{

  private var value: String = "dupl  ;  fifo * lossy"
  var inputArea: html.TextArea = null

  override def spawn(block: Selection[EventTarget]): Unit = {
    val inputDiv = Structure.panelBox(block,"Input (Shift-Enter to update)").append("div")
      .attr("id", "textBox")

    inputDiv.append("textarea")
      .attr("id", "inputArea")
      .attr("class","my-textarea")
      .attr("rows", "10")
      .attr("style", "width: 100%")
      .attr("placeholder", "dupl  ;  fifo * lossy")

    inputArea = dom.document.getElementById("inputArea").asInstanceOf[html.TextArea]

  }

  override def update_local: Option[String] = {
    value = inputArea.value
    DSL.parseWithError(value) match {
      case preo.lang.Parser.Success(result,_) =>
        reolive.State.connector = result
        None
      case preo.lang.Parser.Failure(msg,_) =>
        throw new ParserException(msg)
      case preo.lang.Parser.Error(msg,_) =>
        throw new ParserException(msg)
    }

  }

  override def update_remote: Unit = ???

  override def clear: Unit = ???
}
