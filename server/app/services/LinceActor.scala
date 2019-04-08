package services

import akka.actor._
import hprog.common.ParserException
import hprog.frontend.{SageSolver, Show, Solver}

import sys.process._



object LinceActor {
  def props(out: ActorRef) = Props(new LinceActor(out))
}

class LinceActor(out: ActorRef) extends Actor{
  /**
    * Reacts to messages containing a JSON with a connector (string) and a modal formula (string),
    * produces a mcrl2 model and its LPS from the connector,
    * calls mcrl2 to verify the formula,
    * wraps each into a new JSON (via process),
    * and forwards the result to the "out" actor to generate an info (or error) box.
    */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Get a request to produce a graphic of a hprog.
    * @param msg
    * @return
    */
  private def process(msg: String): String = {
    val cleanMsg = msg.replace("\\\\", "\\")
                      .replace("\\n","\n")

    try {
      //println(s"building trajectory from $cleanMsg")
      val syntax = hprog.DSL.parse(cleanMsg)
//      println("a")
//      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
      val prog = hprog.frontend.Semantics.syntaxToValuation(syntax)

      /////
      // tests: to feed to Sage
//      var sages = List[String]()
//      val systems = Solver.getDiffEqs(syntax)
//      val solver = new SageSolver("/home/jose/Applications/SageMath")
//      solver.batch(systems)
//      for ((eqs,repl) <- solver.cached) {
//        sages ::= s"## Solved(${eqs.map(Show(_)).mkString(", ")})"
////        sages ::= repl.mkString(",")
//      }


      val traj = prog.traj(Map())
//      println(s"b - traj(0)=${traj(0)} - traj(1)=${traj(1)}")
      val max: Double = traj.dur.getOrElse(10)
      val x0 = traj(0)
      var traces =  (x0.keys zip List[List[Double]]())
                   .toMap.withDefaultValue(List())
        //(traj.vars zip List[List[Double]]()).toMap.withDefaultValue(List())
//      println(s"c - max=$max")
      val samples = 0.0 to max by (max / 100)
      for (t: Double <- samples)
        for ((variable, value) <- traj(t))
          traces += variable -> (value::traces(variable))
//      println("d")
      var js = ""
      val rangeTxt = "x: "+samples.mkString("[",",","]")
//      println("e")
      for ((variable, values) <- traces)
        js += s"""var t$variable = {
                 |   $rangeTxt,
                 |   y: ${values.reverse.mkString("[",",","]")},
                 |   mode: 'lines',
                 |   name: '$variable'
                 |};
             """.stripMargin
      js += s"var data = ${traces.keys.map("t"+_).mkString("[",",","]")};" +
        s"\nvar layout = {};" +
        s"\nPlotly.newPlot('graphic', data, layout, {showSendToCloud: true});"
//      println("done:\n"+js)
//      js++"§§"++sages.reverse.mkString("\n")
      js
    }
    catch {
      case p:ParserException =>
        //println("failed parsing: "+p.toString)
        "Error: "+p.toString
      case e:Throwable => "Error "+e.toString
    }

  }




}
