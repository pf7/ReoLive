package reolive

import org.scalajs.dom
import org.singlespaced.d3js.Selection
import preo.ast.{Connector, CoreConnector}

object State {
  type Block = Selection[dom.EventTarget]

  var connector: Connector = _

  var coreConnector: CoreConnector = _

  var error: modules.Error = _
}
