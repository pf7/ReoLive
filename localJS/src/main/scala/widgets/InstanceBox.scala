package widgets

import common.widgets.{ErrorBox, PanelBox}
import preo.DSL
import preo.ast.{Connector, CoreConnector}
import preo.frontend.{Eval, Show, Simplify}

//todo: this should also be shared
class InstanceBox(dependency: PanelBox[Connector], errors: ErrorBox) extends PanelBox[CoreConnector]("Concrete instance", Some(dependency)){
  private var ccon: CoreConnector = _
  private var box: Block = _

  override def get: CoreConnector = ccon

  override def init(div: Block): Unit = {
    box = panelBox(div, true).append("div")
      .attr("id", "instanceBox")
  }

  override def update: Unit = {
    box.text("")
    Eval.unsafeInstantiate(dependency.get) match {
      case Some(reduc) =>
        // GOT A TYPE
        box.append("p")
          .text(Show(reduc) + ":\n  " +
            Show(DSL.unsafeTypeOf(reduc)._1))
        //println(Graph.toString(Graph(Eval.unsafeReduce(reduc))))
        ccon = Eval.unsafeReduce(reduc)
      case _ =>
        // Failed to simplify
        errors.warning("Failed to reduce connector: " + Show(Simplify.unsafe(dependency.get)))
    }
  }
}

