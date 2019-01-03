package widgets

import common.widgets.{Box, OutputArea}
import hprog.ast.{Prog, Trajectory}
import json.Loader
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import preo.frontend.Show

import scala.collection.immutable.NumericRange

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
    println("before eval")
    if (js.startsWith("Error")) errorBox.error(js)
    else scala.scalajs.js.eval(js)
    println("after eval")
  }

  override def update(): Unit = {
    RemoteBox.remoteCall("linceWS",dependency.get,draw)
  }
//    val socket = new WebSocket("ws://localhost:9000/message")
//
//    socket.onmessage = { e: MessageEvent => {process(e.data); socket.close()}}
//    // process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors) }
//
//    socket.addEventListener("open", (e: Event) => {
//      socket.send(dependency.get)
//    })
//  }


//  def process(receivedData: String): Unit =
//  // TODO!!!!

//    typeInfo.text("")
//    instanceInfo.text("")
//
//    val result = Loader(receivedData)
//    //      val result = io.circe.parser.parse(receivedData)
//    // println(result)
//
//    //    println(result)
//    result match {
//      case Right(message) => {
//        errors.error(message)
//      }
//      //        case Right(js:Json) => {
//      //          js.as[ConnectorMsg] match {
//      //            case Left(value) => errors.error(value.getMessage())
//      //            case Right(value) => {
//      //              typeInfo.append("p")
//      ////                .text(Show(value.typ))
//      ////              instanceInfo.append("p")
//      ////                .text(Show(value.con) + ":\n  " +
//      ////                  value.reductyp)
//      ////              ccon = value.con
//      //              this.id = value.id
//      //              reload
//      //            }
//      //          }
//      //        }
//      case Left((typ, reducTyp, con, ide)) => {
//        typeInfo.append("p")
//          .text(typ)
//        instanceInfo.append("p")
//          .text(Show(con) + ":\n  " +
//            reducTyp)
//        ccon = con
//        this.id = ide
//        reload
//      }
//    }


}


