package services

import java.io.{File, PrintWriter}

import akka.actor._
import hprog.ast.Prog
import hprog.common.ParserException

import scala.collection.immutable.NumericRange



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
      println(s"building trajectory from $cleanMsg")
      val prog = hprog.DSL.parse(cleanMsg)
      println("a")
      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
      println(s"b - traj(0)=${traj(0)} - traj(1)=${traj(1)}")
      val max: Double = traj.sup.getOrElse(10)
      var traces = (traj.vars zip List[List[Double]]()).toMap.withDefaultValue(List())
      println(s"c - max=$max")
      val range = 0.0 to max by (max / 100)
      for (t: Double <- range)
        for ((variable, value) <- traj(t))
          traces += variable -> (value::traces(variable))
      println("d")
      var js = ""
      val rangeTxt = "x: "+range.mkString("[",",","]")
      println("e")
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
      println("done:\n"+js)
      js
    }
    catch {
      case p:ParserException =>
        println("failed parsing: "+p.toString)
        "Error: "+p.toString
      case e:Throwable => "Error "+e.toString
    }

  }




}
