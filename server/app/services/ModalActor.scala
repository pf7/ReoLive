package services

import java.io.{File, PrintWriter}

import akka.actor._
import preo.DSL
import preo.ast.Connector
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.Eval
import MCRL2Bind._



object ModalActor {
  def props(out: ActorRef) = Props(new ModalActor(out))
}


class ModalActor(out: ActorRef) extends Actor{
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

  private def process(msg: String): String = {
    val (raw_connector, modal_instance, operation) = JsonLoader.parse(msg)
    if(raw_connector.isEmpty){
      JsonCreater.createError("Parser error: Empty connector").toString
    }
    else{
      if(modal_instance.isEmpty){
        JsonCreater.createError("Parser error: Empty modal logic instance").toString
      }
      else{
        DSL.parseWithError(raw_connector.get) match {
          case preo.lang.Parser.Success(result,_) =>
            try {

              val reduc:Connector = Eval.instantiate(result)
              val coreConnector = Eval.reduce(reduc)

              val model = preo.frontend.mcrl2.Model(coreConnector)

              operation match {
                case Some("view") =>
                  storeInFile(model)

                  // generateLTS
                  callLtsGraph()
                  "ok"
                case Some("check") =>
                  val id = Thread.currentThread().getId
                  storeInFile(model)
                  generateLTS()

                  val file = new File(s"/tmp/modal_$id.mu")
                  file.setExecutable(true)
                  val pw = new PrintWriter(file)
                  pw.write(modal_instance.get)
                  pw.close()

                  val save_output = savepbes()
                  if(save_output._1 == 0)
                    JsonCreater.create(solvepbes()).toString
                  else
                    JsonCreater.createError("Modal Logic failed: " + save_output._2+
                      "\n when parsing\n"+modal_instance.get).toString
                case Some(op) =>
                  JsonCreater.createError("unknown operation: "+op).toString()
                case None =>
                  JsonCreater.createError("no operation found.").toString()
              }

            }
            catch {
              // type error
              case e: TypeCheckException =>
                JsonCreater.createError("Type error: " + e.getMessage).toString

              case e: GenerationException =>
                JsonCreater.createError("Generation failed: " + e.getMessage).toString

              case e: java.io.IOException => // by solvepbes/savepbes/storeInFile/generateLTS
                JsonCreater.createError("IO exception: " + e.getMessage).toString
            }
          case preo.lang.Parser.Failure(emsg,_) =>
            JsonCreater.createError("Parser failure: " + emsg + " in "+raw_connector.get).toString
          //        instanceInfo.append("p").text("-")
          case preo.lang.Parser.Error(emsg,_) =>
            JsonCreater.createError("Parser error: " + emsg + " in "+raw_connector.get).toString
          //        instanceInfo.append("p").text("-")
        }
      }
    }



  }

}
