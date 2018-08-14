//package controllers
//
//import javax.inject._
//import play.api.mvc._
//import play.api.libs.streams.ActorFlow
//import javax.inject.Inject
//import akka.actor.{ActorSystem, Props}
//import akka.stream.Materializer
//import services.ModelActor
//
//
//@Singleton
//class ModelController @Inject()(cc:ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {
//
//  def model = WebSocket.[String, String] { request =>
//    ActorFlow.actorRef { id =>
//      ModelActor.props(id, "mcrl2")
//    }
//  }
//}
//
//
