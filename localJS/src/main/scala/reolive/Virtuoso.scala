package reolive

import common.widgets.virtuoso._
import common.widgets.{Box, GraphBox, OutputArea, Setable}
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import preo.DSL
import preo.ast.{BVal, Connector}
import preo.frontend.Show

import scala.scalajs.js.annotation.JSExportTopLevel

object Virtuoso extends{

  var inputBox: VirtuosoBox = _
  var graphics: GraphBox = _

  var instantiate: VirtuosoInstantiate = _

  var infoBox: VirtuosoInfoBox = _

  var examples: VirtuosoExamplesBox = _
  var errors: OutputArea = _
  var descr: OutputArea = _

  var aut: VirtuosoAutomataBox = _

  var csBox:VirtuosoCSBox = _
  var outputCs:OutputArea = _

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
//    object Description extends Setable[String] {
//      override def setValue(value: String): Unit = {println("hmmm... "+value); errors.warning(value)}
//      override def get: Unit = {}
//      override def init(div: Description.Block, visible: Boolean): Unit = ???
//      override def update(): Unit = ???
//    }
    descr = new OutputArea {
      override def setValue(msg: String): Unit = {clear(); super.setValue(msg)}
    }

    inputBox = new VirtuosoBox(reload(),"port",errors)
    instantiate = new VirtuosoInstantiate(inputBox,errors)
    graphics = new VirtuosoGraphBox(instantiate,errors)
    aut = new VirtuosoAutomataBox(instantiate,errors)
    infoBox = new VirtuosoInfoBox(instantiate,errors)
    examples = new VirtuosoExamplesBox(softReload(),inputBox,descr)
    outputCs = new OutputArea
    csBox = new VirtuosoCSBox(instantiate,"",outputCs)

    inputBox.init(leftColumn,true)
    errors.init(leftColumn)
    descr.init(leftColumn)
    examples.init(leftColumn,true)
    graphics.init(rightColumn,visible = true)
    aut.init(rightColumn,false)
    infoBox.init(leftColumn,false)
    csBox.init(leftColumn,true)
    outputCs.init(leftColumn)

    reload()

  }

//  def typeCheck(cstr:String): Unit = try {
//    val c = common.widgets.virtuoso.VirtuosoParser.parse(cstr).getOrElse(preo.DSL.id)
//
//    val typ = DSL.unsafeCheckVerbose(c)
//    val (_, rest) = DSL.unsafeTypeOf(c)
//    errors.message("Type: "+Show(typ))
//    if (rest != BVal(true))
//      errors.warning(s"Warning: did not check if ${Show(rest)}.")
//  }
//  catch Box.checkExceptions(errors)


  /**
    * Function that parses the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def reload(): Unit = {
    errors.clear()
    descr.clear()
    inputBox.update()

    // temporary code - now in instantiate
//    typeCheck(inputBox.get)

    instantiate.update()
    //    information.update()
//    typeInfo.update()
//    instanceInfo.update()
    graphics.update()
    aut.update()
    infoBox.update()
  }
  private def softReload(): Unit = {
    errors.clear()
    inputBox.update()
//    typeCheck(inputBox.get)
    instantiate.update()
    graphics.update()
    aut.update()
    infoBox.update()
  }

  private def export():Unit = {}

}