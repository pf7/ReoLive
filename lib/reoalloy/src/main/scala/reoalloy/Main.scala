package reoalloy

import edu.mit.csail.sdg.alloy4compiler.ast._
import edu.mit.csail.sdg.alloy4compiler.ast.Sig._
import edu.mit.csail.sdg.alloy4compiler.translator.{A4Options, A4Solution, TranslateAlloyToKodkod}
import edu.mit.csail.sdg.alloy4.A4Reporter.NOP
import edu.mit.csail.sdg.alloy4.Err
import edu.mit.csail.sdg.alloy4viz.VizGUI

object Main {

  def showViz(sol : A4Solution): Unit = {
    try {
      sol.writeXML("instance.xml")
      val viz = new VizGUI(true, "instance.xml", null)
      //load do tema
      //viz.loadThemeFile("./model/Reo.thm")
      viz.loadThemeFile("./lib/reoalloy/model/Reo.thm")
    } catch {
      case e: Err => e.printStackTrace()
    }
  }

  def main(args: Array[String]): Unit = {

    // fifo ; lossy
    val c1 : List[Nodo] = List(new Nodo("fifo",List(1),List(2)),new Nodo("lossy",List(2),List(3)))

    // fifo * lossy
    val c2 : List[Nodo] = List(new Nodo("fifo",List(1),List(2)),new Nodo("lossy",List(3),List(4)))

    //dupl ;  fifo * lossy ; drain
    val c3 : List[Nodo] = List(new Nodo("dupl", List(1), List(2, 3)),
      new Nodo("fifo", List(2), List(4)),
      new Nodo("lossy", List(3), List(5)),
      new Nodo("drain",List(4, 5),List()))

    //sync * sync ; merger ; fifo ; dupl ; fifofull * sync
    val c4 : List[Nodo] = List(
      new Nodo("sync", List(1), List(2)),
      new Nodo("sync", List(3), List(4)),
      new Nodo("node", List(2,4), List(5)),
      new Nodo("fifo",List(5), List(6)),
      new Nodo("node",List(6), List(7, 8)),
      new Nodo("fifofull",List(7), List(9)),
      new Nodo("sync",List(8), List(10))
    )

    //fifo * fifo ; merger ; lossy
    val c5 : List[Nodo] = List(
      new Nodo("fifo", List(1), List(2)),
      new Nodo("fifo", List(3), List(4)),
      new Nodo("node", List(2,4), List(5)),
      new Nodo("lossy",List(5), List(6))
    )

    //(\x. fifo^x) ; merger ; lossy ; fifofull
    val c6 : List[Nodo] = List(
      new Nodo("fifo", List(1), List(2)),
      new Nodo("fifo", List(3), List(4)),
      new Nodo("node", List(2,4), List(5)),
      new Nodo("lossy",List(5), List(6)),
      new Nodo("fifofull", List(6), List(7))
    )

    //Alternator: sync*sync ; dupl*dupl; fifo*drain*id; merger ; sync
    val c7 : List[Nodo] = List(
      new Nodo("sync", List(1), List(2)),
      new Nodo("sync", List(3), List(4)),
      new Nodo("node", List(2), List(5, 6)),
      new Nodo("node", List(4), List(7, 8)),
      new Nodo("fifo", List(5), List(9)),
      new Nodo("drain", List(6, 7), List()),
      new Nodo("sync", List(8), List(10)),
      new Nodo("node", List(9, 10), List(11)),
      new Nodo("sync", List(11), List(12))
    )

    // dupl * merger
    val c8 : List[Nodo] = List(new Nodo("node", List(1, 2), List(3, 4)))

    // dupl; lossy*fifo
    val c9 : List[Nodo] = List(
      new Nodo("node", List(1), List(2,3)),
      new Nodo("lossy",List(2), List(4)),
      new Nodo("fifo", List(3), List(5))
    )

    val generator = new AlloyGenerator()

    var sol = generator.getSolutions(c1)
    //println(generator.modelToString())
    //println(sol.toString)

    sol = generator.getSolutions(c2)
    //println(sol.toString)
    //println(generator.modelToString())

    sol = generator.getSolutions(c3)
    //println(sol.toString)
    //println(generator.modelToString())

    sol = generator.getSolutions(c4)
    //println(sol.toString)

    sol = generator.getSolutions(c5)
    //println(sol.toString)
    //println(generator.modelToString())

    sol = generator.getSolutions(c6)
    //println(sol.toString)

    sol = generator.getSolutions(c7)
    //println(sol.toString)
    //println(generator.modelToString)
    showViz(sol)

    //println(generator.checkProperty(List(), "some Node"))

    //println(generator.instanceToString)

    sol = generator.getSolutions(c8)
    //println(sol.toString)

    //println(generator.checkProperty(c9, "all s : State - last | some s.fire + s.next.fire"))
    println(generator.checkProperty(c9, "some Node"))

  }
}

