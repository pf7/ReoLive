package services

import akka.actor.{Actor, ActorRef, Props}
import ifta.{FExp, Feat}

/**
  * Created by guille on 16/01/2019
  */

object IftaActor {
  def props(out:ActorRef) = Props(new IftaActor(out))
}


class IftaActor(out:ActorRef) extends Actor {

  override def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def process(msg:String):String = {
    msg
  }
}
