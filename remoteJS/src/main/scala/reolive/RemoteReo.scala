package reolive

import common.widgets._
import org.scalajs.dom
import dom.html
import org.singlespaced.d3js.d3
import preo.backend._
import preo.frontend.mcrl2.Model
import preo.ast.CoreConnector
import widgets.TypeInstanceBox

import scalajs.js.annotation.JSExportTopLevel



/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo extends{


  var inputBox: PanelBox[String] = _
  var typeInstanceInfo: PanelBox[CoreConnector] = _
  var errors: ErrorBox = _
  var svg: PanelBox[Graph] = _
  var svgAut: PanelBox[Automata] = _
  var mcrl2Box: PanelBox[Model] = _

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

    val colDiv1 = rowDiv.append("div")
//      .attr("class", "col-sm-4")
      .attr("id", "leftbar")

    colDiv1.append("div")
      .attr("id","dragbar")

    // add InputArea
    inputBox = new InputBox(first_reload)
    inputBox.init(colDiv1)

    errors = new ErrorBox
    errors.init(colDiv1)

    typeInstanceInfo = new TypeInstanceBox(second_reload,inputBox, errors)
    typeInstanceInfo.init(colDiv1)

    val buttonsDiv = new ButtonsBox(first_reload, inputBox.asInstanceOf[InputBox])
    buttonsDiv.init(colDiv1)

    val svgDiv = rowDiv.append("div")
//      .attr("class", "col-sm-8")
      .attr("id", "rightbar")

    svg = new GraphBox(typeInstanceInfo, errors)
    svg.init(svgDiv)

    svgAut = new AutomataBox(typeInstanceInfo, errors)
    svgAut.init(svgDiv)

    mcrl2Box = new ModelBox(typeInstanceInfo)
    mcrl2Box.init(svgDiv)

    first_reload()


  }

  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {

    errors.clear
    inputBox.update
    typeInstanceInfo.update
  }
  private def second_reload(): Unit = {
    svg.update
    svgAut.update
    mcrl2Box.update
  }

}

