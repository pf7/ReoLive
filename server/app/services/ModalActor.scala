package services

import java.io.{File, PrintWriter}

import akka.actor._
import preo.DSL
import preo.ast.Connector
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.Eval

import sys.process._


object ModalActor {
  def props(out: ActorRef) = Props(new ModalActor(out))
}


class ModalActor(out: ActorRef) extends Actor{
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def savepbes(): (Int, String) = {
    val id = Thread.currentThread().getId
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val status = s"lts2pbes /tmp/model_$id.lts /tmp/modal_$id.pbes --formula=/tmp/modal_$id.mu".!(ProcessLogger(stdout append _, stderr append _))
    if(status == 0) (status, stdout.toString)
    else (status, stderr.toString)
  }

  private def solvepbes() = {
    val id = Thread.currentThread().getId
    s"pbes2bool /tmp/modal_$id.pbes".!!
  }

  private def process(msg: String): String = {
    val (raw_connector, modal_instance) = JsonLoader.parse(msg)
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

              model.storeInFile
              model.generateLTS


              val id = Thread.currentThread().getId
              val file = new File(s"/tmp/modal_$id.mu")
              file.setExecutable(true)
              val pw = new PrintWriter(file)
              pw.write(modal_instance.get)
              pw.close()

              val save_output = savepbes()
              if(save_output._1 == 0)
                JsonCreater.create(solvepbes()).toString
              else
                JsonCreater.createError("Modal Logic failed: " + save_output._2).toString

            }
            catch {
              // type error
              case e: TypeCheckException =>
                JsonCreater.createError("Type error: " + e.getMessage).toString

              case e: GenerationException =>
                JsonCreater.createError("Generation failed: " + e.getMessage).toString
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



  }

}
