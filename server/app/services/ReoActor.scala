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

  /**
    * Gets a message, does the server processing, and returns the result as a string (using JSON here)
    * @param msg incomming message with the connector
    * @return type of the connector and an instance (type and core connector) using JSON
    */
  private def process(msgCleaned: String): String = {
    var warnings: List[String] = List()
    val msg = msgCleaned
      .replace("\\\\n","\\±")
      .replace("\\n","\n")
      .replace("\\±","\\\\n")
      .replace("\\\\","\\")
    DSL.parseWithError(msg) match {
      case Right(result) =>
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
//          val id=Thread.currentThread().getId
//          val msg = common.messages.Message.ConnectorMsg(typ,reducType,coreConnector,id)
//          msg.asJson.toString()
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
//      case f@preo.lang.Parser.Failure(_,_) =>
//        JsonCreater.createError("Parser failure: " + f.toString()).toString
//              instanceInfo.append("p").text("-")
//      case preo.lang.Parser.Error(msg,_) =>
      case Left(msg) =>
        JsonCreater.createError("Parser error: " + msg).toString
      //        instanceInfo.append("p").text("-")
    }

  }
}
