package reolive

import common.widgets.virtuoso._
import common.widgets.{Box, GraphBox, OutputArea}
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import preo.DSL
import preo.ast.{BVal, Connector}
import preo.frontend.Show

import scala.scalajs.js.annotation.JSExportTopLevel

object Virtuoso extends{

  var inputBox: VirtuosoBox = _
  var graphics: GraphBox = _

  var instanciate: VirtuosoInstantiate = _

  var infoBox: VirtuosoInfoBox = _

  var examples: VirtuosoExamplesBox = _
  var errors: OutputArea = _

  var aut: VirtuosoAutomataBox = _

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

    aut = new VirtuosoAutomataBox(instanciate,errors)
    aut.init(rightColumn,false)

    infoBox = new VirtuosoInfoBox(instanciate,errors)
    infoBox.init(leftColumn,false)

    reload()

  }

  def typeCheck(cstr:String): Unit = try {
    val c = common.widgets.virtuoso.VirtuosoParser.parse(cstr).getOrElse(preo.DSL.id)

    val typ = DSL.unsafeCheckVerbose(c)
    val (_, rest) = DSL.unsafeTypeOf(c)
    errors.message("Type: "+Show(typ))
    if (rest != BVal(true))
      errors.warning(s"Warning: did not check if ${Show(rest)}.")
  }
  catch Box.checkExceptions(errors)


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    errors.clear()
    inputBox.update()

    // temporary code
    typeCheck(inputBox.get)

    instanciate.update()
    //    information.update()
//    typeInfo.update()
//    instanceInfo.update()
    graphics.update()
    aut.update()
    infoBox.update()
  }

  private def export():Unit = {}

}