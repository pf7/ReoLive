package services

import akka.actor.{Actor, ActorRef, Props}
import ifta.common.ParseException
import ifta.{DSL, FExp, Feat}


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
    println("about to call parser")
    try {
      var fe:FExp = DSL.parseFexp(msg)
      fe.products(fe.feats.toSet).map(p => p.mkString("(",",",")")).mkString("(",",",")")
    } catch {
      case p:ParseException => println("failed parsing: "+p.toString +"\nMessage was: " +msg)
        "Error: "+ p.toString
      case e:Throwable => "Error: "+ e.toString
    }

  }
}
