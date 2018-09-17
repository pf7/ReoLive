package reolive

import common.widgets._
import org.scalajs.dom
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets._

import scala.scalajs.js.annotation.JSExportTopLevel
import hprog.ast.Prog


/**
  * Created by jose on 1/09/2018.
  */
object Lince extends{

  var inputBox: Box[String] = _
//  var typeInfo: PanelBox[Connector] = _
  var information: Box[Prog] = _
  var errors: ErrorArea = _

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

    // add InputArea
    inputBox = new InputBox(reload(),default = "v:=0;p:=0;p=v,v=10&p<=0 /\\ v<=0 ; v:=v* -0.5",id = "Lince",rows=3)
    inputBox.init(leftColumn,true)

    errors = new ErrorArea(id="Lince")
    errors.init(leftColumn)

//    typeInfo = new TypeBox(inputBox, errors)
//    typeInfo.init(colDiv1,true)

    information = new HProgBox(inputBox, errors)
    information.init(rightColumn,true)

    reload()

  }


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    errors.clear()
    inputBox.update()
    information.update()
  }

}
