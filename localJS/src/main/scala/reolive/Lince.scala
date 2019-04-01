package reolive

import common.widgets.Lince.{LinceBox, LinceExamplesBox}
import common.widgets._
import org.scalajs.dom
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets._

import scala.scalajs.js.annotation.JSExportTopLevel
import hprog.ast.Syntax


/**
  * Created by jose on 1/09/2018.
  */
object Lince extends{

  var inputBox: LinceBox = _
//  var information: Box[Syntax] = _
  var examples: LinceExamplesBox = _
  var graphics: GraphicBox = _
  var errors: OutputArea = _
  var descr: OutputArea = _


  @JSExportTopLevel("reolive.Lince.main")
  def main(content: html.Div): Unit = {


    // Creating outside containers:
    val contentDiv = d3.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
        .attr("id", "mytable")

    val leftColumn = rowDiv.append("div")
    //      .attr("class", "col-sm-4")
        .attr("id", "leftbar")
        .attr("class", "leftside")

    leftColumn.append("div")
        .attr("id","dragbar")
        .attr("class", "middlebar")

    val rightColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

    // add description area
    descr = new OutputArea
    errors = new OutputArea //(id="Lince")
    inputBox = new LinceBox(reload(),"",errors)
    examples = new LinceExamplesBox(softReload(),inputBox,descr)
    graphics = new GraphicBox(inputBox,errors)

    inputBox.init(leftColumn,true)
    errors.init(leftColumn)
    examples.init(leftColumn,true)
    descr.init(leftColumn)
    graphics.init(rightColumn,visible = true)

    // load default button
    if (!examples.loadButton("Bounce")) {
      reload()
    }

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    descr.clear()
    softReload()
  }

  private def softReload(): Unit = {
    errors.clear()
    inputBox.update()
//    information.update()
    graphics.update()
  }

}
