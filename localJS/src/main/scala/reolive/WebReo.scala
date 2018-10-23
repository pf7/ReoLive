package reolive

import org.scalajs.dom
import dom.html
import org.singlespaced.d3js.d3
import preo.backend._
import preo.ast.{Connector, CoreConnector}
import preo.frontend.mcrl2.Model
import common.widgets._
import widgets._

import scalajs.js.annotation.JSExportTopLevel


/**
  * Created by jose on 27/04/2017.
  */
object WebReo extends{

  var inputBox: Box[String] = _
  var typeInfo: Box[Connector] = _
  var instanceInfo: Box[CoreConnector] = _
  var logicBox: Box[String] = _
  var errors: ErrorArea = _
  var svg: Box[Graph] = _
  var svgAut: Box[Automata] = _
  var mcrl2Box: Box[Model] = _
  var outputLogic: ErrorArea = _


  @JSExportTopLevel("reolive.WebReo.main")
  def main(content: html.Div): Unit = {

    //    // add header
    //    d3.select(content).append("div")
    //      .attr("id", "header")
    //      .append("h1").text("Reo Live - Connector Families")

    val contentDiv = d3.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
        .attr("id", "mytable_wr")

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
    inputBox = new InputCodeBox(reload(), default="dupl  ;  fifo * lossy", id="wr", rows=4)

    errors      = new ErrorArea(id="wr")
    outputLogic = new ErrorArea(id="wrLog")

    typeInfo = new TypeBox(inputBox, errors)

    instanceInfo = new InstanceBox(typeInfo, errors)

    logicBox = new LogicBox(instanceInfo,outputLogic)

    val buttonsDiv = new ButtonsBox(reload(), inputBox.asInstanceOf[InputCodeBox])

    svg = new GraphBox(instanceInfo, errors)

    svgAut = new AutomataBox(instanceInfo, errors)

    mcrl2Box = new Mcrl2Box(instanceInfo,errors)

    // place items
    inputBox.init(leftside,visible = true)
    errors.init(leftside)
    typeInfo.init(leftside,visible = true)
    instanceInfo.init(leftside,visible = true)
    buttonsDiv.init(leftside,visible = false)
    logicBox.init(leftside,visible = true)
    outputLogic.init(leftside)

    svg.init(rightside,visible = true)
    svgAut.init(rightside,visible = false)
    mcrl2Box.init(rightside,visible = false)

    reload()


  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    errors.clear
    inputBox.update
    typeInfo.update
    instanceInfo.update

    svg.update
    svgAut.update
    mcrl2Box.update
  }

}
