package reolive

import D3Lib.GraphsToJS
import org.scalajs.dom
import org.scalajs.dom.html
import preo.ast.CoreConnector
import preo.backend._
import org.singlespaced.d3js.Ops._

import scalatags.JsDom.all._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.singlespaced.d3js.d3

//todo-tomorrow: create the example that the student did in this lang



@JSExport
object WebReoD3 {
    def graph: Graph = {
      val node1 = ReoNode(1, Option(null), Source, Option(null))
      val node2 = ReoNode(2, Option(null), Mixed, Option(null))
      val node3 = ReoNode(3, Option(null), Mixed, Option(null))
      val node4 = ReoNode(4, Option(null), Mixed, Option(null))
      val node5 = ReoNode(5, Option(null), Mixed, Option(null))
      val node6 = ReoNode(6, Option(null), Mixed, Option(null))
      val node7 = ReoNode(7, Option(null), Sink, Option(null))

      val link1 = ReoChannel(1, 2,NoArrow, NoArrow, "fifo", Option(null))
      val link2 = ReoChannel(3, 5,NoArrow, NoArrow, "fifo", Option(null))
      val link3 = ReoChannel(2, 3,NoArrow, NoArrow, "dup", Option(null))
      val link4 = ReoChannel(2, 4,NoArrow, NoArrow, "dup", Option(null))
      val link5 = ReoChannel(4, 6,NoArrow, NoArrow, "lossy", Option(null))
      val link6 = ReoChannel(5, 7,NoArrow, NoArrow, "merger", Option(null))
      val link7 = ReoChannel(6, 7,NoArrow, NoArrow, "merger", Option(null))

      Graph(List(link1, link2, link3, link4, link5, link6, link7), List(node1, node2, node3, node4, node5, node6, node7))
    }


  @JSExport
  def main(content: html.Div) = {


    scalajs.js.eval(GraphsToJS(this.graph))
  }
}

//todo-1: Vamos começar por dar a martelada nisto e resolver o problema do D3 inicial.
//todo-1: Vamos começar por procurar remover a martelada. Fazemos clone do scalajsD3 e vamos implementar o force
//todo-2: vamos ver se conseguimos juntar isso aqui e fazer o código funcionar
