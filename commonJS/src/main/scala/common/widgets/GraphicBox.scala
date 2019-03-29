package widgets

import common.widgets.{Box, OutputArea}
import hprog.common.ParserException
import hprog.frontend.{Show, Solver}

class GraphicBox(dependency: Box[String], errorBox: OutputArea)
  extends Box[Unit]("Trajectories", List(dependency)) {
  var box : Block = _
  override def get: Unit = {}

  //  private val widthCircRatio = 7
  //  private val heightCircRatio = 3
  //  private val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = super.panelBox(div,visible,
      buttons = List(
        Right("glyphicon glyphicon-refresh")-> (()=>update(),"Load the program (shift-enter)")
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

  def draw(js: String): Unit = {
//        println("before eval")
    if (js.startsWith("Error")) errorBox.error(js)
    else scala.scalajs.js.eval(js)
//        println("after eval")
  }

  override def update(): Unit = {
    //RemoteBox.remoteCall("linceWS",dependency.get,draw)
    try {
//      println(s"building trajectory from $cleanMsg")
      val syntax = hprog.DSL.parse(dependency.get)
      //      println("a")
      //      val (traj,_) = hprog.ast.Trajectory.hprogToTraj(Map(),prog)
      val prog = hprog.frontend.Semantics.syntaxToValuation(syntax)
      //
      // tests: to feed to Sage
      val eqs = Solver.getDiffEqs(syntax)
      for (e <- eqs)
        errorBox.message(s"- ${e.map(Show(_)).mkString(", ")}:\n${hprog.frontend.Semantics.genSage(e)}")
      //
      val traj = prog.traj(Map())
      //      println(s"b - traj(0)=${traj(0)} - traj(1)=${traj(1)}")
      val max: Double = traj.dur.getOrElse(10)
      val x0 = traj(0)
      var traces =  (x0.keys zip List[List[Double]]())
        .toMap.withDefaultValue(List())
      //(traj.vars zip List[List[Double]]()).toMap.withDefaultValue(List())
      //      println(s"c - max=$max")
      val samples = if (max<=0) List(0.0) else 0.0 to max by (max / 100)
      for (t: Double <- samples)
        for ((variable, value) <- traj(t))
          traces += variable -> (value::traces(variable))
      //      println("d")
      var js = ""
      val rangeTxt = "x: "+samples.mkString("[",",","]")
      //      println("e")
      for ((variable, values) <- traces)
        js += s"""var t$variable = {
                 |   $rangeTxt,
                 |   y: ${values.reverse.mkString("[",",","]")},
                 |   mode: 'lines',
                 |   name: '$variable'
                 |};
             """.stripMargin
      js += s"var data = ${traces.keys.map("t"+_).mkString("[",",","]")};" +
        s"\nvar layout = {};" +
        s"\nPlotly.newPlot('graphic', data, layout, {showSendToCloud: true});"
      //println("done:\n"+js)
      draw(js)
    }
    catch {
      case p:ParserException =>
        errorBox.error(p.toString)
      case e:Throwable =>
        errorBox.error(e.toString)

    }
  }

}