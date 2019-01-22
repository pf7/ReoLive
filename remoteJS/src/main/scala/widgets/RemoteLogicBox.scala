package widgets

import common.widgets.{Box, CodeBox, LogicBox, OutputArea}
import json.Loader
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.{Formula, Model}
import preo.lang.ParserUtils

class RemoteLogicBox(connectorStr: Box[String], default:String, connector: Box[CoreConnector], outputBox: OutputArea)
  extends LogicBox(connector,default,outputBox){
//  extends Box[String]("Modal Logic", List(connector)) with CodeBox {



//
////  private var code: scalajs.js.Dynamic = _
////  private val boxId = "modalInputArea"
//
//  //  var input: String = default
//  var model: Model = _
//  var operation: String = "check"
//
////  override def get: String = input
//
//  override def init(div: Block, visible: Boolean): Unit = {
//    val inputDiv = panelBox(div,visible /*List("padding-right"->"25pt")*/ /*, 80*/
//        ,buttons = List(
//          Right("glyphicon glyphicon-refresh")-> (()=>reload("check"),"Check if the property holds (shift-enter)"),
//          Left("View")-> (()=>reload("view"),"View mCRL2 behaviour using ltsgraph"),
//          Left("MA")   -> (()=> debugNames(), "Map actions in the formula to sets of actions in the mCRL2 specification")
//        ))
//      .append("div")
//      .attr("id", "modalBox")
//
//    inputDiv.append("textarea")
//      .attr("id", boxId)
//      .attr("name", boxId)
//      .attr("class","my-textarea prettyprint lang-java")
//      //      .attr("rows", rows.toString)
//      .attr("style", "width: 100%; max-width: 100%; min-width: 100%;")
//
//    buildCodeArea(default)
//
//    val realTxt = dom.document.getElementById("modalBox")
//      .childNodes(1).childNodes(0).childNodes(0).asInstanceOf[html.TextArea]
//    realTxt.onkeydown = {e: dom.KeyboardEvent =>
//      if(e.keyCode == 13 && e.shiftKey){e.preventDefault(); reload("check")}
//      else ()
//    }
//  }
//
////  todo: this function can be centralized. maybe....
//  override def update(): Unit = {
//    model = Model(connector.get)
//    super.update()
//  }
//
//
//
  private def callMcrl2(): Unit = {
    val msg = s"""{ "modal": "$input","""+
              s""" "connector" : "${connectorStr.get}", """+
              s""" "operation" : "$operation" }"""
    RemoteBox.remoteCall("modal",msg,process)
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
//
//  private def debugNames(): Unit = {
//    update()
//    outputBox.clear()
//    if (connector.get==null)
//      outputBox.message("null model...")
//    else
//      outputBox.message(model.getMultiActionsMap
//        .map(kv => kv._1+":"+kv._2.map("\n - "+_).mkString(""))
//        .mkString("\n"))
//  }
//
//  /**
//    * Called by the ModalBox when pressed the button or shift-enter.
//    * Triggers the ModalBox to query the server and process the reply.
//    */
//  private def reload(op:String): Unit = {
//    outputBox.clear()
//    operation = op
//    update()
//    callMcrl2()
//  }
//  override protected var input: String = default
//  override protected val boxId: String = "modalInputArea"
  private var operation: String = "check"

  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
            Right("glyphicon glyphicon-refresh")-> (()=>reload(),"Check if the property holds (shift-enter)"),
            Left("View")-> (()=>doOperation("view"),"View mCRL2 behaviour using ltsgraph"),
            Left("MA")   -> (()=> debugNames(), "Map actions in the formula to sets of actions in the mCRL2 specification")
          )

  override def reload(): Unit = doOperation("check")

  private def doOperation(op:String): Unit = {
    outputBox.clear()
    operation = op
    update()
    callMcrl2()
  }

//  override protected val codemirror: String = _
}
