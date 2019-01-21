package reolive

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
  var graphics: Box[Unit] = _
  var errors: OutputArea = _

  @JSExportTopLevel("reolive.Lince.main")
  def main(content: html.Div): Unit = {


    // Creating outside containers:
    val contentDiv = d3.select(content).append("div")
      .attr("class", "content")

    val rowDiv = contentDiv.append("div")
//      .attr("class", "row")
        .attr("id", "mytable_Lince")

    val leftColumn = rowDiv.append("div")
    //      .attr("class", "col-sm-4")
        .attr("id", "leftbar_Lince")
        .attr("class", "leftside")

    leftColumn.append("div")
        .attr("id","dragbar_Lince")
        .attr("class", "middlebar")

    val rightColumn = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar_Lince")
      .attr("class", "rightside")

    errors = new OutputArea //(id="Lince")

    // add InputArea
    inputBox = new LinceBox(reload(),"v:=5; p:=10; c:=0;\nwhile (c<4) {\n  v=-9.8, p=v & p<0 /\\ v<0;\n  v:=-0.5*v; c:=c+1\n}",errors)
    inputBox.init(leftColumn,true)
    errors.init(leftColumn)

    //    typeInfo = new TypeBox(inputBox, errors)
//    typeInfo.init(colDiv1,true)

    examples = new LinceExamplesBox(reload(),inputBox)
    examples.init(leftColumn,true)

//    information = new HProgBox(inputBox, errors)
//    information.init(leftColumn,true)

    graphics = new GraphicBox(inputBox,errors)
    graphics.init(rightColumn,visible = true)

    reload()

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    errors.clear()
    inputBox.update()
//    information.update()
    graphics.update()
  }

}
