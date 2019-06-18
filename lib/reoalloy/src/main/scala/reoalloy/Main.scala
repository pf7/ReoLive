package reoalloy

object Main {


  def main(args: Array[String]): Unit = {

    // fifo ; lossy
    val c1 : List[Connector] = List(new Connector("fifo",List(1),List(2)),new Connector("lossy",List(2),List(3)))

    // fifo * lossy
    val c2 : List[Connector] = List(new Connector("fifo",List(1),List(2)),new Connector("lossy",List(3),List(4)))

    //dupl ;  fifo * lossy ; drain
    val c3 : List[Connector] = List(new Connector("dupl", List(1), List(2, 3)),
      new Connector("fifo", List(2), List(4)),
      new Connector("lossy", List(3), List(5)),
      new Connector("drain",List(4, 5),List()))

    //sync * sync ; merger ; fifo ; dupl ; fifofull * sync
    val c4 : List[Connector] = List(
      new Connector("sync", List(1), List(2)),
      new Connector("sync", List(3), List(4)),
      new Connector("node", List(2,4), List(5)),
      new Connector("fifo",List(5), List(6)),
      new Connector("node",List(6), List(7, 8)),
      new Connector("fifofull",List(7), List(9)),
      new Connector("sync",List(8), List(10))
    )

    //fifo * fifo ; merger ; lossy
    val c5 : List[Connector] = List(
      new Connector("fifo", List(1), List(2)),
      new Connector("fifo", List(3), List(4)),
      new Connector("node", List(2,4), List(5)),
      new Connector("lossy",List(5), List(6))
    )

    //(\x. fifo^x) ; merger ; lossy ; fifofull
    val c6 : List[Connector] = List(
      new Connector("fifo", List(1), List(2)),
      new Connector("fifo", List(3), List(4)),
      new Connector("node", List(2,4), List(5)),
      new Connector("lossy",List(5), List(6)),
      new Connector("fifofull", List(6), List(7))
    )

    //Alternator: sync*sync ; dupl*dupl; fifo*drain*id; merger ; sync
    val c7 : List[Connector] = List(
      new Connector("sync", List(1), List(2)),
      new Connector("sync", List(3), List(4)),
      new Connector("node", List(2), List(5, 6)),
      new Connector("node", List(4), List(7, 8)),
      new Connector("fifo", List(5), List(9)),
      new Connector("drain", List(6, 7), List()),
      new Connector("sync", List(8), List(10)),
      new Connector("node", List(9, 10), List(11)),
      new Connector("sync", List(11), List(12))
    )

    // dupl * merger
    val c8 : List[Connector] = List(new Connector("node", List(1, 2), List(3, 4)))

    // dupl; lossy*fifo
    val c9 : List[Connector] = List(
      new Connector("node", List(1), List(2,3)),
      new Connector("lossy",List(2), List(4)),
      new Connector("fifo", List(3), List(5))
    )

    // vdupl; lossy*fifo
    val c10 : List[Connector] = List(
      new Connector("vdupl", List(1), List(2,3)),
      new Connector("lossy",List(2), List(4)),
      new Connector("fifo", List(3), List(5))
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
    println(generator.modelToString)
    generator.showViz(sol)

    //println(generator.checkProperty(List(), "some Node"))

    sol = generator.getSolutions(c8)
    //println(sol.toString)

    //println(generator.checkProperty(c9, "all s : State - last | some s.fire + s.next.fire"))
    //println(generator.checkProperty(c9, "some Node"))

    sol = generator.getSolutions(c10)
    //generator.showViz(sol)
    //println(generator.modelToString)

  }
}

