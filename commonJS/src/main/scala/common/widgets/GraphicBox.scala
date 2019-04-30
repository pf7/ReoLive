package widgets

import common.widgets.{Box, OutputArea}
import hprog.backend.{Show, TrajToJS}
import hprog.common.ParserException
import hprog.frontend.Semantics.Valuation
import hprog.frontend.{Solver, Traj}

class GraphicBox(program: Box[String], eps: Box[String], errorBox: OutputArea)
  extends Box[Unit]("Trajectories", List(program)) {
  var box : Block = _
  override def get: Unit = {}
  private var trajectory: Option[Traj[Valuation]] = None

  //  private val widthCircRatio = 7
  //  private val heightCircRatio = 3
  //  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
//        Right("glyphicon glyphicon-refresh")-> (()=>update(),"Load the program (shift-enter)"),
          Right("glyphicon glyphicon-refresh") -> (() => draw(None,true), "Draw again the image"),
          Left("resample") -> (() => redraw(true), "Resample: draw again the image, using the current zooming window"),
          Left("all jumps") -> (() => redraw(false), "Resample and include all boundary nodes")
        //        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      ))
    box.append("div")
      .attr("id", "graphic")
  }


  def draw(range:Option[(Double,Double)],hideCont:Boolean): Unit = {
    trajectory match {
      case Some(traj) =>
        val js = TrajToJS(traj,range,hideCont)
        //println(s"done\n${js}")
        scala.scalajs.js.eval(js)
      case None =>
        errorBox.warning("No trajectory found to draw.")
    }
  }

  override def update(): Unit = {
    //RemoteBox.remoteCall("linceWS",dependency.get,draw)
    try {
      //println(s"building trajectory from  ${program.get}")
      //println(s"building trajectory using ${eps.get}")
      val syntax = hprog.DSL.parse(program.get)
      val epsVal: Double = try {eps.get.toDouble}
        catch {
          case e:Throwable =>
            errorBox.error(e.getMessage)
            0
        }
      //      println("a")
      //      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
      val prog = hprog.frontend.Semantics.syntaxToValuationTaylor(syntax,eps=epsVal)

      // tests: to feed to Sage
      val eqs = Solver.getDiffEqs(syntax)
      for (e <- eqs) if (e.nonEmpty)
        errorBox.message(s"- ${e.map(Show(_)).mkString(", ")}" )
          //s"\n${hprog.frontend.SageSolver.genSage(e)}")

      trajectory = Some(prog.traj(Map()))
      //      println(s"b - traj(0)=${traj(0)} - traj(1)=${traj(1)}")

      draw(None,true)
    }
    catch {
      case p:ParserException =>
        errorBox.error(p.toString)
      case e:Throwable =>
        errorBox.error(e.toString)

    }
  }

  def redraw(hideCont:Boolean): Unit = {
    var range:Option[(Double,Double)] = None
    try {
      val reply = scalajs.js.Dynamic.global.layout.xaxis.range.toString
      reply.split(',') match {
        case Array(s1, s2) => range = Some(s1.toDouble, s2.toDouble)
        case _ => errorBox.error(s"Unexpected layout: $reply")
      }
    }
    catch Box.checkExceptions(errorBox,"Graphic")

    draw(range,hideCont:Boolean)
  }

}