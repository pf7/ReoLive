package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.{Inject, _}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.AlloyActor

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class AlloyController @Inject()(cc:ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {


  def socket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef { out =>
      AlloyActor.props(out)
    }
  }
}