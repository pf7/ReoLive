package services

import java.io.File

import akka.actor._
import play.mvc.Results
import preo.DSL
import preo.backend.{Automata, Graph, PortAutomata}
import preo.common.{GenerationException, TypeCheckException}

object ModelActor {
  def props(out: ActorRef, typ: String) = Props(new ModelActor(out, typ))
}

class ModelActor(out: ActorRef, typ: String) extends Actor {
  def receive = {
    case msg: String => if(typ == "mcrl2") {
      val file = new File(s"/tmp/model_$msg.mcrl2")
      if(file.exists()) {
        Results.ok(file, "model.mcrl2")
        out ! "success"
      }
      else{
        out ! "404"
      }
    }
  }
}
