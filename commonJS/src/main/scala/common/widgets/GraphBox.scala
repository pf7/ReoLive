package common.widgets

import common.frontend.GraphsToJS
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Graph

class GraphBox(dependency: PanelBox[CoreConnector]) extends PanelBox[Graph]("Circuit of the instance", Some(dependency)) {
  var graph: Graph = _
  var box: Block = _
  override def get: Graph = graph

  private val widthCircRatio = 7
  private val heightCircRatio = 3
  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block): Unit = {
    box = PanelBox.appendSvg(super.panelBox(div,true),"circuit")
    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {(e: MouseEvent) => if(!isVisible) drawGraph else deleteDrawing}

  }

  override def update: Unit = {
    if(isVisible) {
      drawGraph
    }
  }

  private def drawGraph: Unit = {
    graph = Graph(dependency.get)
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")
    scalajs.js.eval(GraphsToJS(graph))
  }

    private def deleteDrawing: Unit = {
      box.selectAll("g").html("")
    }
}
