package reolive

import common.widgets._
import org.scalajs.dom.html
import org.singlespaced.d3js.d3
import widgets._

import scala.scalajs.js.annotation.JSExportTopLevel


/**
  * Created by jose on 27/04/2017.
  */
object RemoteAlloy extends{


  private var inputBox: PreoBox = _
  private var errors: OutputArea = _
  private var descr: OutputArea = _

  private var alloyBox: AlloyBox = _


  @JSExportTopLevel("reolive.RemoteAlloy.main")
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
    leftside.append("div")
      .attr("id","dragbar")
      .attr("class", "middlebar")

    val rightside = rowDiv.append("div")
      //      .attr("class", "col-sm-8")
      .attr("id", "rightbar")
      .attr("class", "rightside")

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
    // must be after inputbox and mcrl2box
    alloyBox = new AlloyBox(inputBox, errors)
    val buttonsDiv =
      new ButtonsBox(soft_reload(), List(inputBox, new OutputArea, descr))


    // place boxes

    inputBox.init(leftside,true)
    errors.init(leftside)
    buttonsDiv.init(leftside,false)
    descr.init(leftside)
    alloyBox.init(rightside,true)

    // default button
//    if (args.isEmpty && buttonsDiv.loadButton("dupl;lossy*fifo")) {
      //println("args: " + args.mkString("-"))
      first_reload()
//    }

  }

  /**
    * Called by InputBox.
    * Parse the expressions written in the input box and
    * tests if they're valid and generates the output if they are.
    */
  private def first_reload(): Unit= {
    descr.clear()
    errors.clear()
    inputBox.update()
    alloyBox.update()
  }

  private def soft_reload(): Unit= {
    errors.clear()
    inputBox.update()
    alloyBox.update()
  }

//  /**
//    * Called by AlloyBox upon producing a new value.
//    */
//  private def second_reload(): Unit = {
//    alloyBox.update
//  }

  private def export(): Unit = {
    val loc = scalajs.js.Dynamic.global.window.location
    val ori = loc.origin.toString
    val path = loc.pathname.toString
    val hash = loc.hash.toString
    val search = common.Utils.buildSearchUri(List("c"->inputBox.get))

    errors.clear()
    errors.message(ori+path+search+hash)
  }

}

