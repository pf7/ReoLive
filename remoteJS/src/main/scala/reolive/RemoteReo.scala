package reolive

import common.widgets._
import org.scalajs.dom
import dom.html
import org.singlespaced.d3js.d3
import preo.backend._
import preo.frontend.mcrl2.Model
import preo.ast.CoreConnector
import widgets.{LogicBox, OutputArea, RemoteModelBox, RemoteInstanceBox}

import scalajs.js.annotation.JSExportTopLevel



/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo extends{


  var inputBox: Box[String] = _
  var typeInstanceInfo: RemoteInstanceBox = _
  var errors: ErrorArea = _

  var modalBox: Box[String] = _
  var outputBox: OutputArea = _

  var svg: Box[Graph] = _
  var svgAut: Box[Automata] = _
  var mcrl2Box: RemoteModelBox = _

  @JSExportTopLevel("reolive.RemoteReo.main")
  def main(content: html.Div): Unit = {

    //    // add header
    //    d3.select(content).append("div")
    //      .attr("id", "header")
    //      .append("h1").text("Reo Live - Connector Families")

    val contentDiv = d3.select(content).append("div")
      .attr("id", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
      .attr("id", "mytable")

    val leftside = rowDiv.append("div")
//      .attr("class", "col-sm-4")
      .attr("id", "leftbar_wr")
      .attr("class", "leftside")
    leftside.append("div")
      .attr("id","dragbar_wr")
      .attr("class", "middlebar")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar_wr")
      .attr("class", "rightside")

    // add InputArea
    inputBox = new InputBox(first_reload(), default="dupl  ;  fifo * lossy", id="wr",rows=4)
    inputBox.init(leftside,true)

    errors = new ErrorArea
    errors.init(leftside)


    typeInstanceInfo = new RemoteInstanceBox(second_reload(),inputBox, errors)
    typeInstanceInfo.init(leftside,true)

    val buttonsDiv = new ButtonsBox(first_reload(), inputBox.asInstanceOf[InputBox])
    buttonsDiv.init(leftside,false)

    outputBox = new OutputArea()

    modalBox = new LogicBox(third_reload(), inputBox, outputBox)
    modalBox.init(leftside,true)

    outputBox.init(leftside)

    svg = new GraphBox(typeInstanceInfo, errors)
    svg.init(rightside,true)

    svgAut = new AutomataBox(typeInstanceInfo, errors)
    svgAut.init(rightside,false)

    mcrl2Box = new RemoteModelBox(typeInstanceInfo, errors)
    mcrl2Box.init(rightside,false)


    first_reload()


  }

  /**
    * Called by InputBox.
    * Parse the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {
    errors.clear
    inputBox.update
    typeInstanceInfo.update
  }

  /**
    * Called by TypeInstance upon producing a new value.
    * Retrieve the instance from the TypeInstance widget and
    * triggers the circuit, svg, and mCRL2 generation.
    */
  private def second_reload(): Unit = {
    mcrl2Box.id = typeInstanceInfo.id
    svg.update
    svgAut.update
    mcrl2Box.update
  }

  /**
    * Called by the ModalBox when pressed the button or shift-enter.
    * Triggers the ModalBox to query the server and process the reply.
    */
  private def third_reload(): Unit = {
    outputBox.clear
    modalBox.update
  }

}

