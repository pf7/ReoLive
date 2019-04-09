package reolive

import common.widgets.Ifta.IFTABox
import common.widgets._
import org.scalajs.dom
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets._

import scala.scalajs.js.annotation.JSExportTopLevel


/**
  * Created by jose on 27/04/2017.
  */
object RemoteReo2 extends{


  private var inputBox: PreoBox = _
  private var typeInstanceInfo: RemoteInstanceBox = _
  private var errors: OutputArea = _
  private var descr: OutputArea = _

  private var modalBox: RemoteLogicBox = _
  private var outputBox: OutputArea = _

  private var svg: GraphBox = _
  private var svgAut: AutomataBox = _
  private var mcrl2Box: RemoteModelBox = _
  private var ifta: RemoteIFTABox = _
  private var iftaAut: IFTABox =_
  private var uppaalAut:RemoteUppaalAutBox = _
  private var uppaalNet:RemoteUppaalNetBox = _

  @JSExportTopLevel("reolive.RemoteReo2.main")
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
      .attr("id", "leftbar")
      .attr("class", "leftside")
      .style("width","50%")
    leftside.append("div")
      .attr("id","dragbar")
      .attr("class", "middlebar")
      .style("margin-left","50%")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")
      .style("width","50%")

    // configure defaults
    val search = scalajs.js.Dynamic.global.window.location.search.asInstanceOf[String]
    val args = common.Utils.parseSearchUri(search)
    val conn = args.getOrElse("c", "dupl  ;  fifo * lossy")
    val form = args.getOrElse("f", "<all*> <fifo> true")


    // Create boxes (order matters)
    errors =
      new OutputArea
    descr = new OutputArea {
      override def setValue(msg: String): Unit = {clear(); if(msg.nonEmpty) super.setValue(msg)}
    }
    inputBox =
      new PreoBox(first_reload(), export, conn, errors)
    typeInstanceInfo =
      new RemoteInstanceBox(second_reload(),inputBox, errors)
    svg =
      new GraphBox(typeInstanceInfo, errors) {
        override protected val widthCircRatio = 7
        override protected val heightCircRatio = 6
      }
    svgAut =
      new AutomataBox(typeInstanceInfo, errors) {
        override protected val widthAutRatio = 7
        override protected val heightAutRatio = 6
      }
    mcrl2Box =
      new RemoteModelBox(typeInstanceInfo, errors)
    outputBox = new OutputArea()
    // must be after inputbox and mcrl2box
    modalBox = new RemoteLogicBox(inputBox, form, typeInstanceInfo, outputBox)
    val buttonsDiv =
      new ButtonsBox(soft_reload(), List(inputBox, modalBox, descr))

    iftaAut =
      new IFTABox(typeInstanceInfo, errors) {
        override protected val widthAutRatio = 7
        override protected val heightAutRatio = 6
      }
    ifta =
      new RemoteIFTABox(typeInstanceInfo,iftaAut,svg,errors)
    uppaalAut =
      new RemoteUppaalAutBox(typeInstanceInfo,errors)
    uppaalNet =
      new RemoteUppaalNetBox(typeInstanceInfo,errors)


    svg.init(leftside,true)
    errors.init(leftside)
    inputBox.init(leftside,true)
    descr.init(leftside)
    ifta.init(leftside,visible = false)
    typeInstanceInfo.init(leftside,false)
    buttonsDiv.init(leftside,false)

    svgAut.init(rightside,true)
    iftaAut.init(rightside,visible=false)
    modalBox.init(rightside,true)
    outputBox.init(rightside)
    mcrl2Box.init(rightside,false)
    uppaalAut.init(rightside,visible = false)
    uppaalNet.init(rightside,visible = false)

    // default button
    if (args.isEmpty && buttonsDiv.loadButton("dupl;lossy*fifo")) {
      //println("args: " + args.mkString("-"))
      first_reload()
    }

  }

  /**
    * Called by InputBox.
    * Parse the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {
    descr.clear()
    errors.clear
    inputBox.update
    typeInstanceInfo.update
  }

  private def soft_reload(): Unit= {
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
    uppaalAut.update
    uppaalNet.update
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

