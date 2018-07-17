package common.widgets

import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import org.singlespaced.d3js.d3
import preo.ast.CoreConnector
import preo.frontend.mcrl2.Model

class ModelBox(dependency: PanelBox[CoreConnector]) extends PanelBox[Model]("mCRL2 of the instance", Some(dependency))  {
  private var box: Block = _
  private var model: Model = _

  override def get: Model = model

  override def init(div: Block): Unit = {
    box = panelBox(div, false).append("div")
      .attr("id", "mcrl2Box")


    dom.document.getElementById("mCRL2 of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { (e: MouseEvent) => if (!isVisible) produceMcrl2 else deleteModel}
  }

  override def update: Unit = if(isVisible) produceMcrl2

  private def produceMcrl2: Unit = {
    model = Model(dependency.get)
    box.html(model.webString)
  }

  private def deleteModel: Unit = {
    box.html("")
  }

}
