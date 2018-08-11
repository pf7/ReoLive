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

  var inputBox: PanelBox[String] = _
  var typeInfo: PanelBox[Connector] = _
  var instanceInfo: PanelBox[CoreConnector] = _
  var errors: ErrorBox = _
  var svg: PanelBox[Graph] = _
  var svgAut: PanelBox[Automata] = _
  var mcrl2Box: PanelBox[Model] = _

  @JSExportTopLevel("reolive.WebReo.main")
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

    val colDiv1 = rowDiv.append("div")
    //      .attr("class", "col-sm-4")
        .attr("id", "leftbar")

    colDiv1.append("div")
        .attr("id","dragbar")

    // add InputArea
    inputBox = new InputBox(reload)
    inputBox.init(colDiv1)

    errors = new ErrorBox
    errors.init(colDiv1)

    typeInfo = new TypeBox(inputBox, errors)
    typeInfo.init(colDiv1)

    instanceInfo = new InstanceBox(typeInfo, errors)
    instanceInfo.init(colDiv1)

    val buttonsDiv = new ButtonsBox(reload, inputBox.asInstanceOf[InputBox])
    buttonsDiv.init(colDiv1)

    val svgDiv = rowDiv.append("div")
//      .attr("class", "col-sm-8")
        .attr("id", "rightbar")


    svg = new GraphBox(instanceInfo, errors)
    svg.init(svgDiv)

    svgAut = new AutomataBox(instanceInfo, errors)
    svgAut.init(svgDiv)

    mcrl2Box = new ModelBox(instanceInfo)
    mcrl2Box.init(svgDiv)

    reload()


  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit={

    errors.clear
    inputBox.update
    typeInfo.update
    instanceInfo.update

    svg.update
    svgAut.update
    mcrl2Box.update
  }

}
