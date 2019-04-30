package reolive

import common.widgets.Lince.{LinceBox, LinceExamplesBox}
import common.widgets._
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets.RemoteGraphicBox

import scala.scalajs.js.annotation.JSExportTopLevel

object RemoteLince {

  object Lince extends{

    var inputBox: LinceBox = _
    //  var typeInfo: PanelBox[Connector] = _
    //var information: Box[Syntax] = _
    var examples: LinceExamplesBox = _
    var graphic: RemoteGraphicBox = _
    var errors: OutputArea = _
    var descr: OutputArea = _
    var deviation: InputBox = _


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
//      inputBox = new InputBox(reload(),default = "x:=2;y:=1;\n  x=2*x,\n  y=y & x>10",id = "Lince",rows=3)
      descr = new OutputArea
      errors = new OutputArea //(id="Lince")
      inputBox = new LinceBox(reload(), "",errors)
      examples = new LinceExamplesBox(softReload(),inputBox,descr)
      //information = new LinceInfoBox(inputBox, errors)
      deviation = new InputBox(reloadGraphics(),"0","deviation",1,
        title = "Deviation warnings",
        refreshLabel = "Add warnings when conditions would differ when deviating the variables by some epsilon > 0. Set to 0 to ignore these warnings.")
      graphic= new RemoteGraphicBox(inputBox, deviation, errors)

      inputBox.init(leftColumn,true)
      errors.init(leftColumn)
      examples.init(leftColumn,true)
      descr.init(leftColumn)
      deviation.init(leftColumn,visible = false)
      //information.init(rightColumn,true)
      graphic.init(rightColumn,true)

      //    typeInfo = new TypeBox(inputBox, errors)
      //    typeInfo.init(colDiv1,true)


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
      //information.update()
      deviation.update()
      graphic.update()
    }

    private def reloadGraphics(): Unit = {
      errors.clear()
      deviation.update()
      graphic.resample(hideCont = true)
    }

  }

}
