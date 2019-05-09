package services

import akka.actor._
import preo.backend.Network
import preo.frontend.Eval


object AlloyActor {
  def props(out:ActorRef) = Props(new AlloyActor(out))
}

class AlloyActor(out: ActorRef) extends Actor{
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
      .replace("\\n", "\n")

    try {
      val genConn = preo.DSL.parse(cleanMsg)
      val conn = Eval.reduce(genConn)
      val netw: Network = preo.backend.Network.apply(conn,hideClosed = false)

      // Create ReoAlloy magic here

      "Resulting Alloy Program"
   }
    catch {
      case e: Throwable => "Error: "+e.getMessage
    }

  }

//  private def callSage(prog: String, sagePath:String): String = try {
//    val syntax = hprog.DSL.parse(prog)
//    val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
//    val replySage = hprog.frontend.SageSolver.callSageSolver(eqs,sagePath,timeout=10)
//    replySage.mkString("§")
//  }
//  catch {
//    case p:ParserException =>
//      //println("failed parsing: "+p.toString)
//      s"Error: When parsing ${prog} - ${p.toString}"
//    case e:Throwable => "Error "+e.toString
//  }



}
