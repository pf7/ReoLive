package reolive

import common.widgets.virtuoso._
import common.widgets.{GraphBox, OutputArea}
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import preo.frontend.Show

import scala.scalajs.js.annotation.JSExportTopLevel

object Virtuoso extends{

  var inputBox: VirtuosoBox = _
  var graphics: GraphBox = _

  var instanciate: VirtuosoInstantiate = _

  var examples: VirtuosoExamplesBox = _
  var errors: OutputArea = _

  @JSExportTopLevel("reolive.Virtuoso.main")
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

    errors = new OutputArea

    // add InputArea
    inputBox = new VirtuosoBox(reload(),"fifo",errors)
    inputBox.init(leftColumn,true)
    errors.init(leftColumn)

    //    typeInfo = new TypeBox(inputBox, errors)
    //    typeInfo.init(colDiv1,true)

    examples = new VirtuosoExamplesBox(reload(),inputBox)
    examples.init(leftColumn,true)

    //    information = new HProgBox(inputBox, errors)
    //    information.init(leftColumn,true)

    instanciate = new VirtuosoInstantiate(inputBox,errors)

//    typeInfo     = new TypeBox(inputBox, errors)     // do not place
//    instanceInfo = new InstanceBox(typeInfo, errors) // do not place
//    typeInfo.init(rightColumn,visible = true)
//    instanceInfo.init(rightColumn,visible = true)


    graphics = new VirtuosoGraphBox(instanciate,errors)
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

    // temporary code
    val c = common.widgets.virtuoso.VirtuosoParser.parse(inputBox.get).getOrElse(preo.DSL.id)
    errors.message(Show(c)+": "+preo.DSL.unsafeTypeCheck(c))

    instanciate.update()
    //    information.update()
//    typeInfo.update()
//    instanceInfo.update()
    graphics.update()
  }

  private def export():Unit = {}

}