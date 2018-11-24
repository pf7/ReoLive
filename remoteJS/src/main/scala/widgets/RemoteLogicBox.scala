package widgets

import common.widgets.{Box, LogicBox, OutputArea}
import json.Loader
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.{Formula, Model}
import preo.lang.{FormulaParser, ParserUtils}

class RemoteLogicBox(connectorStr: Box[String], connector: Box[CoreConnector], outputBox: OutputArea)
  extends LogicBox(connector,outputBox){

  //    extends Box[String]("Modal Logic", List(formulaStr,connector)){


//  private val default = "<all>[fifo] false"
//
//  private var code: scalajs.js.Dynamic = _
//  private val boxId = "modalInputArea"

  //  var input: String = default
  var model: Model = _
  var operation: String = "check"

//  var inputAreaDom: html.TextArea = _

//  override def get: String = input

  override def init(div: Block, visible: Boolean): Unit = {
    val inputDiv = panelBox(div,visible /*List("padding-right"->"25pt")*/ /*, 80*/
        ,buttons = List(
          Right("glyphicon glyphicon-refresh")-> (()=>reload("check")),
          Left("View")-> (()=>reload("view")),
          Left("MA")   -> (()=> debugNames())
        ))
      .append("div")
      .attr("id", "modalBox")

//    inputDiv.append("textarea")
    ////      .attr("id", boxId)
    ////      .attr("class","my-textarea")
    ////      .attr("rows", "3")
    ////      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
    ////      .attr("placeholder", input)
    ////
    ////    val inputAreaDom = dom.document.getElementById(boxId).asInstanceOf[html.TextArea]
    ////
    ////    inputAreaDom.onkeydown = {e: dom.KeyboardEvent =>
    ////      if(e.keyCode == 13 && e.shiftKey) {
    ////        e.preventDefault(); reload("check")
    ////      }
    ////      else ()
    ////    }
    inputDiv.append("textarea")
      .attr("id", boxId)
      .attr("name", boxId)
      .attr("class","my-textarea prettyprint lang-java")
      //      .attr("rows", rows.toString)
      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")

    buildCodeArea(default)

    val realTxt = dom.document.getElementById("modalBox")
      .childNodes(1).childNodes(0).childNodes(0).asInstanceOf[html.TextArea]
    realTxt.onkeydown = {e: dom.KeyboardEvent =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload("check")}
      else ()
    }
  }

//  todo: this function can be centralized. maybe....
  override def update(): Unit = {
//    val inputAreaDom = dom.document.getElementById("modalInputArea").asInstanceOf[html.TextArea]
//    //    if(input != default || inputAreaDom.value != "")
//    //      input = LogicBox.expandFormula(inputAreaDom.value,connector.get)
//    model = Model(connector.get)
//    if (inputAreaDom.value != "")
//      input = inputAreaDom.value
    model = Model(connector.get)
    super.update()
  }



  private def callMcrl2(): Unit = {
    val socket = new WebSocket("ws://localhost:9000/modal")
    // parse formula
//    val modalForm = LogicBox.expandFormula(input,model)
    val modalForm = input //parseFormula

    // send request to process
    socket.onmessage = { e: MessageEvent => {process(e.data.toString); socket.close()}}// process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors) }

    // build JSON and send it through the socket
    socket.addEventListener("open", (e: Event) => {
      val string:String =
        s"""{ "modal": "${modalForm
          .replace("\\","\\\\")
          .replace("\n","\\n")}","""+
        s""" "connector" : "${connectorStr.get
          .replace("\\","\\\\")
          .replace("\n","\\n")}", """+
        s""" "operation" : "$operation" }"""
      socket.send(string)
    })
  }

  def process(receivedData: String): Unit = {
    if (receivedData != "ok") {
      val result = Loader.loadModalOutput(receivedData)

      result match {
        case Right(message) =>
          outputBox.error(message)
        case Left(message) =>
          val res = message.filterNot(_=='\n')
          outputBox.message("- "+res+" -")
          ParserUtils.parseFormula(input) match {
            case Left(err) => outputBox.error(err)
            case Right(form) =>
              val prefixes = Formula.notToHide(form).filterNot(_==Nil)
              if (prefixes.nonEmpty)
                outputBox.warning(s"Exposing ${prefixes.map(_.map(_.name).mkString("[", "/", "]")).mkString(",")}")
              ParserUtils.hideBasedOnFormula(form,connector.get) match {
                case Left(err) => outputBox.error(err)
                case Right((_,formExpanded)) =>
                  outputBox.warning("Expanded formula:\n" + formExpanded)
              }
          }
      }
    }
  }

  private def debugNames(): Unit = {
    update()
    outputBox.clear()
    if (connector.get==null)
      outputBox.message("null model...")
    else
      outputBox.message(model.getMultiActionsMap
        .map(kv => kv._1+":"+kv._2.map("\n - "+_).mkString(""))
        .mkString("\n"))
  }

  /**
    * Called by the ModalBox when pressed the button or shift-enter.
    * Triggers the ModalBox to query the server and process the reply.
    */
  private def reload(op:String): Unit = {
    outputBox.clear()
    operation = op
    update()
    callMcrl2()
  }

}
