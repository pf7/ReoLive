package widgets

import common.widgets.{Box, OutputArea}

class RemoteGraphicBox(dependency: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Trajectories", List(dependency)) {
  var box : Block = _
  override def get: Unit = {}

//  private val widthCircRatio = 7
//  private val heightCircRatio = 3
//  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("glyphicon glyphicon-refresh")-> (()=>update(),"Load the program (shift-enter)"),
        Left("resample")-> (() => resample(),"Resample: draw again the image, using the current zooming window")
//        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))
    box.append("div")
       .attr("id", "graphic")

//    traj = Trajectory.hprogToTraj(Map(),dependency.get)._1

    //    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
//      .onclick = {e: MouseEvent => if(!isVisible) drawGraph() else deleteDrawing()}
  }

//  override def update(): Unit = if(isVisible) {
//    deleteDrawing()
//    drawGraph()
//  }

  def draw(reply: String): Unit = {
//    println("before eval")
    errorBox.clear()
    reply.split("§§").toList match {
      case js::rest =>
        if (js.startsWith("Error")) errorBox.error(js)
        else scala.scalajs.js.eval(js)
        rest match {
          case sages :: _ => errorBox.warning("Results from SageMath:\n"+sages)
          case _ =>
        }
      //    println("after eval")
      case x =>
        errorBox.error(s"unexpected reply from LinceWS: $x")
    }
  }

  override def update(): Unit = {
    errorBox.message("Waiting for SageMath...")
    RemoteBox.remoteCall("linceWS",dependency.get,draw)
  }

  def resample(): Unit = {
    var range:String = ""
    try range = scalajs.js.Dynamic.global.layout.xaxis.range.toString
    catch Box.checkExceptions(errorBox, "Graphic")

    errorBox.message("Redrawing. Waiting for SageMath...")
    RemoteBox.remoteCall("linceWS",s"§redraw $range, ${dependency.get}",draw)
  }

}


