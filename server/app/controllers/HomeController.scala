package controllers

import java.io.File
import scala.concurrent._
import ExecutionContext.Implicits.global
import javax.inject._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `sbtroutes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index())
  }

//  def about = Action {
//    Ok(views.html.about())
//  }
//
//  def remote = Action{
//    Ok(views.html.remote())
//  }

  def onlineReo = Action{
    Ok(views.html.onlineReo())
  }

  def onlineReo2 = Action{
    Ok(views.html.onlineReo2())
  }

  def onlineLince = Action{
    Ok(views.html.onlineLince())
  }

  def model(id: Long) = Action{
    val file = new File(s"/tmp/model_$id.mcrl2")
    if(file.exists())
      Ok.sendFile(file, fileName = _ => "model.mcrl2")
    else
      fileNotFound()
  }

  def lps(id: Long) = Action{
    val file = new File(s"/tmp/model_$id.lps")
    if(file.exists())
      Ok.sendFile(file, fileName = _ => "model.lps")
    else
      fileNotFound()
  }

  def lts(id: Long) = Action{
    val file = new File(s"/tmp/model_$id.lts")
    if(file.exists())
      Ok.sendFile(file, fileName = _ => "model.lts")
    else
      fileNotFound()
  }


  def fileNotFound() = NotFound("File not found! \nPerform an update and try again.")
}
