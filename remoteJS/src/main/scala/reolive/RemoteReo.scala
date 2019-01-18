package reolive

import common.widgets._
import org.scalajs.dom
import dom.html
import org.singlespaced.d3js.d3
import preo.backend._
import preo.frontend.mcrl2.Model
import preo.ast.CoreConnector
import reolive.RemoteReo.outputBox
import widgets.{RemoteIFTABox, RemoteInstanceBox, RemoteLogicBox, RemoteModelBox}

import scalajs.js.annotation.JSExportTopLevel



/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo extends{


  private var inputBox: InputCodeBox = _
  private var typeInstanceInfo: RemoteInstanceBox = _
  private var errors: OutputArea = _

  private var modalBox: RemoteLogicBox = _
  private var outputBox: OutputArea = _

  private var svg: GraphBox = _
  private var svgAut: AutomataBox = _
  private var mcrl2Box: RemoteModelBox = _
  private var ifta: RemoteIFTABox = _
  private var iftaAut: IFTABox =_

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

    val leftside = rowDiv.append("div")
//      .attr("class", "col-sm-4")
      .attr("id", "leftbar_wr")
      .attr("class", "leftside")
    leftside.append("div")
      .attr("id","dragbar_wr")
      .attr("class", "middlebar")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar_wr")
      .attr("class", "rightside")

    // configure defaults
    val search = scalajs.js.Dynamic.global.window.location.search.asInstanceOf[String]
    val args = common.Utils.parseSearchUri(search)
    val conn = args.getOrElse("c", "dupl  ;  fifo * lossy")
    val form = args.getOrElse("f", "<all*> <fifo> true")


    // Create boxes (order matters)
    inputBox =
      new InputCodeBox(first_reload(), export, conn, id="wr",rows=4)
    errors =
      new OutputArea
    typeInstanceInfo =
      new RemoteInstanceBox(second_reload(),inputBox, errors)
    svg =
      new GraphBox(typeInstanceInfo, errors)
    svgAut =
      new AutomataBox(typeInstanceInfo, errors)
    mcrl2Box =
      new RemoteModelBox(typeInstanceInfo, errors)
    outputBox = new OutputArea()
    // must be after inputbox and mcrl2box
    modalBox = new RemoteLogicBox(inputBox, form, typeInstanceInfo, outputBox)
    val buttonsDiv =
      new ButtonsBox(first_reload(), inputBox.asInstanceOf[InputCodeBox],modalBox)
    iftaAut =
      new IFTABox(typeInstanceInfo, errors)
    ifta =
      new RemoteIFTABox(typeInstanceInfo,iftaAut,errors)

    inputBox.init(leftside,true)
    errors.init(leftside)
    typeInstanceInfo.init(leftside,true)
    buttonsDiv.init(leftside,false)
    modalBox.init(leftside,true)
    outputBox.init(leftside)
    ifta.init(leftside,visible = false)
    svg.init(rightside,true)
    svgAut.init(rightside,false)
    mcrl2Box.init(rightside,false)
    iftaAut.init(rightside,visible=false)



    first_reload()
  }

  /**
    * Called by InputBox.
    * Parse the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {
    errors.clear
    inputBox.update
    typeInstanceInfo.update
  }

  /**
    * Called by TypeInstance upon producing a new value.
    * Retrieve the instance from the TypeInstance widget and
    * triggers the circuit, svg, and mCRL2 generation.
    */
  private def second_reload(): Unit = {
    mcrl2Box.id = typeInstanceInfo.id
    svg.update
    svgAut.update
    mcrl2Box.update
    ifta.update
    iftaAut.update
  }

  private def export(): Unit = {
    val loc = scalajs.js.Dynamic.global.window.location
    val ori = loc.origin.toString
    val path = loc.pathname.toString
    val hash = loc.hash.toString
    val search = common.Utils.buildSearchUri(List("c"->inputBox.get,"f"->modalBox.get))

    errors.clear()
    errors.message(ori+path+search+hash)
  }

}

