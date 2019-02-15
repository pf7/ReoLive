package common.widgets.virtuoso

import common.frontend.GraphsToJS
import common.widgets.{Box, GraphBox, OutputArea}
import preo.ast.CoreConnector
import preo.backend.Graph


/**
  * Created by guillecledou on 31/01/2019
  */


class VirtuosoGraphBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends GraphBox(dependency,errorBox) {

//  override  def toJs(g:Graph):Unit = scalajs.js.eval(GraphsToJS(g))

  override protected def drawGraph(): Unit = try{
    graph =  Graph.connToVirtuosoGraph(dependency.get,true)//Graph.connToNodeGraph(dependency.get,true)
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")
    //println("Drawing graph - source: "+dependency.get)
    //println("Drawing graph - produced: "+ graph)
//        toJs(graph)
        scalajs.js.eval(GraphsToJS.toVirtuosoJs(graph))
  }
  catch Box.checkExceptions(errorBox)
}
