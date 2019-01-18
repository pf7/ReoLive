package services

import akka.actor.{Actor, ActorRef, Props}
import ifta.common.ParseException
import ifta.{DSL, FExp, Feat}
import play.api.libs.json.{JsDefined, JsString, JsValue, Json}

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
    val (fmS,featsS) = parseMsg(msg)
    try {
      var fm:FExp = DSL.parseFexp(fmS)
      var feats:Set[String] = DSL.parserFeats(featsS)
      fm.products(feats).map(p => p.mkString("(",",",")")).mkString("(",",",")")
    } catch {
      case p:ParseException =>
        println("Failed parsing: "+p.toString +"\nMessage was: " +msg)
        "Error: "+ p.toString
      case e:Throwable => "Error: "+ e.toString
    }
  }

  private def parseMsg(msg:String):(String,String) = {
    val res:JsValue = Json.parse(msg)
    val fm:String = (res \ "fm").get.asInstanceOf[JsString].value
    val feats:String = (res \ "feats").get.asInstanceOf[JsString].value
    (fm,feats)
  }
}
