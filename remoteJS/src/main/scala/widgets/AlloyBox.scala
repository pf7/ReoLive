package widgets

import common.widgets.Ifta.IFTABox
import common.widgets.{Box, GraphBox, OutputArea}
import ifta.backend.{IftaAutomata, Show}
import ifta.{DSL, Feat, NIFTA}
import org.scalajs.dom
import org.scalajs.dom.raw.MouseEvent
import org.scalajs.dom.{EventTarget, html}
import preo.ast.CoreConnector
import preo.backend.Network.Mirrors
import preo.backend.{Automata, Circuit}

import scala.scalajs.js.UndefOr

/**
  * Created by guille on 16/01/2019
  */


class AlloyBox(progr:Box[String], errorBox:OutputArea)
  extends Box[String]("Reo Alloy",List(progr)){

  private var box:Block = _
  private var content:String = ""
  private var mirrors:Mirrors = _
  override def get: String = content

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div,visible)
      .append("div")
      .attr("id","reoAlloy")

    dom.document.getElementById("Reo Alloy").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e : MouseEvent => if (!isVisible) update() else update()}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = {
    println("sending request for Alloy servive")
    RemoteBox.remoteCall("alloyWS", progr.get, process)
  }

  private def process(reply:String): Block = {
    println("got reply: "+reply)
    box.html(reply)
  }


//  try {
//
//      val ...
//
//      iftaAut = Automata[IftaAutomata](progr.get,mirrors)
//
//      var nifta:NIFTA = NIFTA(iftaAut.nifta)
//      var fmInfo =  s"""{ "fm":     "${Show(nifta.fm)}", """ +
//                    s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""
//
//      RemoteBox.remoteCall("ifta", fmInfo, showProducts)
//
//    } catch {
//      case e:Throwable =>
//        errorBox.error(e.getMessage)
//    }



}
