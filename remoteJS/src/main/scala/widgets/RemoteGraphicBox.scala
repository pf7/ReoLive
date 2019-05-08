package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.Syntax
import hprog.backend.TrajToJS
import hprog.frontend.Solver

class RemoteGraphicBox(program: Box[String], eps: Box[String], errorBox: OutputArea)
    extends Box[Unit]("Trajectories", List(program)) {
  var box : Block = _
  private var lastSolver:Option[Solver] = None
  private var lastSyntax:Option[Syntax] = None

  override def get: Unit = {}

//  private val widthCircRatio = 7
//  private val heightCircRatio = 3
//  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("glyphicon glyphicon-refresh")-> (()=>redraw(None,hideCont = true),"Reset zoom and redraw (shift-enter)"),
        Left("resample")  -> (() => resample(hideCont = true), "Resample: draw again the image, using the current zooming window"),
        Left("all jumps") -> (() => resample(hideCont = false),"Resample and include all boundary nodes")
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

  def draw(sageReply: String): Unit = {
    //println("before eval")
    errorBox.clear()
    //errorBox.message(s"got reply: ${sageReply}")
    if (sageReply startsWith "Error")
      errorBox.error(sageReply)
    else try {
      //println(s"got reply from sage: ${sageReply}. About to parse ${dependency.get}.")
      // repeating parsing work done at the server
      val syntax = hprog.DSL.parse(program.get)
      //println("parsed...")
      lastSyntax = Some(syntax)
      val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
      //println("got diffEqs")
      val solver = new hprog.frontend.SageSolverStatic(eqs,sageReply.split('§'))
      //println("got static solver")
      lastSolver = Some(solver)
      redraw(None,hideCont = true)
//      val prog = hprog.frontend.Semantics.syntaxToValuation(syntax,solver)
//      val traj = prog.traj(Map())
//      val js = TrajToJS(traj)
//      scalajs.js.eval(js)
    }
    catch Box.checkExceptions(errorBox, "Trajectories")

//    sageReply.split("§").toList match {
//      case js::rest =>
//        if (js.startsWith("Error")) errorBox.error(js)
//        else scala.scalajs.js.eval(js)
//        rest match {
//          case sages :: _ => errorBox.warning("Results from SageMath:\n"+sages)
//          case _ =>
//        }
//      //    println("after eval")
//      case x =>
//        errorBox.error(s"unexpected reply from LinceWS: $x")
//    }
  }

  private def redraw(range: Option[(Double,Double)],hideCont:Boolean): Unit = try {
    (lastSyntax,lastSolver) match {
      case (Some(syntax),Some(solver)) =>
        val e = getEps
        val prog = hprog.frontend.Semantics.syntaxToValuation(syntax,solver,getEps)
        val traj = prog.traj(Map())
        val js = TrajToJS(traj,range,hideCont)
        scalajs.js.eval(js)
      case _ => errorBox.error("Nothing to redraw.")
    }
  }
  catch Box.checkExceptions(errorBox,"Trajectories")

  override def update(): Unit = {
    errorBox.message("Waiting for SageMath...")
    RemoteBox.remoteCall("linceWS",program.get,draw)
  }

  def resample(hideCont:Boolean): Unit = {
    var range:String = ""
    try range = scalajs.js.Dynamic.global.layout.xaxis.range.toString
    catch Box.checkExceptions(errorBox, "Graphic")

    range.split(",", 2) match {
      case Array(v1, v2) =>
          redraw(Some(v1.toDouble, v2.toDouble),hideCont)
      case Array() => redraw(None,hideCont)
      case _ => errorBox.error(s"Error: Unexpected range: $range.")
    }
//    errorBox.message("Redrawing. Waiting for SageMath...")
//    RemoteBox.remoteCall("linceWS",s"§redraw $range, ${dependency.get}",draw)
  }

  private def getEps: Double = try {
    eps.get.toDouble
  }
  catch {
    case e: Throwable =>
      errorBox.error(e.getMessage)
      0.0
  }

}


