package reolive

import common.widgets._
import hprog.ast.Syntax
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets.RemoteGraphicBox

import scala.scalajs.js.annotation.JSExportTopLevel

object RemoteLince {

  object Lince extends{

    var inputBox: Box[String] = _
    //  var typeInfo: PanelBox[Connector] = _
    var information: Box[Syntax] = _
    var graphic: Box[Unit] = _
    var errors: OutputArea = _

    @JSExportTopLevel("reolive.RemoteLince.main")
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

      // add InputArea
//      inputBox = new InputBox(reload(),default = "v:=0;p:=0;p=v,v=10&p<=0 /\\ v<=0 ; v:=v* -0.5",id = "Lince",rows=3)
      inputBox = new InputBox(reload(),default = "x:=2;y:=1;\n  x=2*x,\n  y=y & x>10",id = "Lince",rows=3)
      inputBox.init(leftColumn,true)

      errors = new OutputArea //(id="Lince")
      errors.init(leftColumn)

      //    typeInfo = new TypeBox(inputBox, errors)
      //    typeInfo.init(colDiv1,true)

      information = new LinceInfoBox(inputBox, errors)
      information.init(rightColumn,true)

      graphic= new RemoteGraphicBox(inputBox, errors)
      graphic.init(rightColumn,true)

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
      graphic.update()
    }

  }

}
