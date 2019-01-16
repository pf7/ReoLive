package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import services.IftaActor

/**
  * Created by guillecledou on 16/01/2019
  */

@Singleton
class IFTAController @Inject()(cc:ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      IftaActor.props(out)
    }
  }

}
