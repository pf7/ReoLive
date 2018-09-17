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
  var errors: ErrorArea = _
  var svg: Box[Graph] = _
  var svgAut: Box[Automata] = _
  var mcrl2Box: Box[Model] = _

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
    inputBox = new InputBox(reload(), default="dupl  ;  fifo * lossy", id="wr", rows=4)
    inputBox.init(leftside,true)

    errors = new ErrorArea(id="wr")
    errors.init(leftside)

    typeInfo = new TypeBox(inputBox, errors)
    typeInfo.init(leftside,true)

    instanceInfo = new InstanceBox(typeInfo, errors)
    instanceInfo.init(leftside,true)

    val buttonsDiv = new ButtonsBox(reload(), inputBox.asInstanceOf[InputBox])
    buttonsDiv.init(leftside,false)

    svg = new GraphBox(instanceInfo, errors)
    svg.init(rightside,true)

    svgAut = new AutomataBox(instanceInfo, errors)
    svgAut.init(rightside,false)

    mcrl2Box = new Mcrl2Box(instanceInfo,errors)
    mcrl2Box.init(rightside,false)

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
