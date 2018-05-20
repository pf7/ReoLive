package modules

import org.scalajs.dom
import org.singlespaced.d3js.Selection


trait Module {

  def spawn(block: Selection[dom.EventTarget]): Unit

  def update_local: Option[String]

  def update_remote: Unit

  def clear: Unit
}
