package widgets

import common.widgets.{ErrorArea, Box}
import json.Loader
import org.scalajs.dom
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import org.scalajs.dom.{EventTarget, html}
import preo.ast.CoreConnector
import preo.frontend.Show

import scala.scalajs.js.UndefOr

class LogicBox(reload: => Unit, dependency: Box[String], outputBox: OutputArea)  extends Box[String]("Modal Logic", Some(dependency)){
  var input: String = "<dupl1out1fifo2in1 | dupl1out2lossy3in1 | dupl1in1 > true"

  var inputAreaDom: html.TextArea = _

  override def get: String = input

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = super.panelBox(div,visible /*List("padding-right"->"25pt")*/ /*, 80*/
        ,buttons = List(Right("glyphicon glyphicon-refresh")-> (()=>reload)))
      .append("div")
      .attr("id", "modalBox")

    val inputArea = inputDiv.append("textarea")
      .attr("id", "modalInputArea")
      .attr("class","my-textarea")
      .attr("rows", "3")
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
      .attr("placeholder", input)

    inputAreaDom = dom.document.getElementById("modalInputArea").asInstanceOf[html.TextArea]

    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload }
      else ()
    }
  }

  //todo: this function can be centralized. maybe....
  override def update: Unit = {
    val inputAreaDom = dom.document.getElementById("modalInputArea").asInstanceOf[html.TextArea]
    if(input != "<dupl1out1fifo2in1 | dupl1out2lossy3in1 | dupl1in1 > true" || inputAreaDom.value != "")
      input = inputAreaDom.value

    val socket = new WebSocket("ws://localhost:9000/modal")

    socket.onmessage = { e: MessageEvent => {process(e.data.toString); socket.close()}}// process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors) }

    socket.addEventListener("open", (e: Event) => {
      val string:String =
        s"""{ "modal": "$input","""+
        s""" "connector" : "${dependency.get
          .replace("\\","\\\\")
          .replace('\n',' ')}" }"""
      socket.send(string)
    })
  }

  def process(receivedData: String): Unit = {

    val result = Loader.loadModalOutput(receivedData)

    result match {
      case Right(message) => {
        outputBox.error(message)
      }
      case Left(message) => {
        outputBox.message(message)
      }
    }
  }
}
