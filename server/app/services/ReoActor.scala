package services


import akka.actor._

import preo.DSL
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.Eval
import MCRL2Bind._



object ReoActor {
  def props(out: ActorRef) = Props(new ReoActor(out))
}

class ReoActor(out: ActorRef) extends Actor {
  /**
    * Reacts to messages containing a connector,
    * wraps each into a JSON (via process),
    * and forwards the result to the "out" actor.
    */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def process(msg: String): String = {
    var warnings: List[String] = List()
    println(msg)
    DSL.parseWithError(msg) match {
      case preo.lang.Parser.Success(result,_) =>
        try {
          val typ = DSL.checkVerbose(result)


          val reduc = Eval.instantiate(result)
          val reducType = DSL.typeOf(reduc)
          val coreConnector = Eval.reduce(reduc)

          val model = preo.frontend.mcrl2.Model(coreConnector)

          storeInFile(model)
          //generateLPS (called by generateLTS)
//          generateLTS

          JsonCreater.create(typ, reducType, coreConnector).toString
        }
        catch {
          // type error
          case e: TypeCheckException =>
            JsonCreater.createError("Type error: " + e.getMessage).toString

          case e: GenerationException =>
            JsonCreater.createError("Generation failed: " + e.getMessage).toString

          case e: java.io.IOException => // by generateLPS/LTS/storeInFile
            JsonCreater.createError("IO exception: " + e.getMessage).toString
          }
      case preo.lang.Parser.Failure(msg,_) =>
        JsonCreater.createError("Parser failure: " + msg).toString
      //        instanceInfo.append("p").text("-")
      case preo.lang.Parser.Error(msg,_) =>
        JsonCreater.createError("Parser error: " + msg).toString
      //        instanceInfo.append("p").text("-")
    }

  }
}
