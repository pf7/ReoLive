package modules
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Selection
import preo.DSL
import preo.common.GenerationException
import preo.frontend.{Eval, Show, Simplify}
import reolive.{State, Structure}

class Instance extends Module {

  private var instanceArea: Selection[EventTarget] = null

  override def spawn(block: Selection[EventTarget]): Unit =
    instanceArea = Structure.panelBox(block,"Concrete instance").append("div")
      .attr("id", "instanceBox")

  override def update_local: Option[String] = {
    Eval.unsafeInstantiate(State.connector) match {
      case Some(reduc) =>
        // GOT A TYPE
        instanceArea.append("p")
          .text(Show(reduc) + ":\n  " +
            Show(DSL.unsafeTypeOf(reduc)._1))
        //println(Graph.toString(Graph(Eval.unsafeReduce(reduc))))
        State.coreConnector = Eval.unsafeReduce(reduc)
        None
      case _ =>
        Some("Failed to reduce connector: "+Show(Simplify.unsafe(State.connector)))
    }
  }

  override def update_remote: Unit = ???

  override def clear: Unit = instanceArea.text("")
}
