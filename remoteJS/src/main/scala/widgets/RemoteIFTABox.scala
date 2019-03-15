package widgets

import common.backend.{CCToFamily, NReoIFTA}
import common.widgets.Ifta.IFTABox
import common.widgets.{Box, OutputArea}
import ifta.{DSL, IFTA, NIFTA}
import ifta.backend.{IftaAutomata, Show}
import org.scalajs.dom
import org.scalajs.dom.{EventTarget, html}
import org.scalajs.dom.raw.MouseEvent
import preo.ast.CoreConnector
import preo.backend.Automata

import scala.scalajs.js.UndefOr

/**
  * Created by guille on 16/01/2019
  */


class RemoteIFTABox(dependency:Box[CoreConnector], iftaAut:IFTABox, errorBox:OutputArea)
  extends Box[NReoIFTA]("IFTA Products",List(dependency)){

  private var solutionsBox: Block = _
  private var nrifta:NReoIFTA = _

  override def get: NReoIFTA = nrifta

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    solutionsBox = panelBox(div,visible)
      .append("div")
      .attr("id","iftaProducts")

    dom.document.getElementById("IFTA Products").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e : MouseEvent => if (!isVisible) solveFm else deleteProducts}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if (isVisible) solveFm


  private def solveFm():Unit ={
    try {
//      var rifta = CCToFamily.toRifta(dependency.get)
      var nifta = NIFTA(Automata[IftaAutomata](dependency.get).nifta)
      var fmInfo =  s"""{ "fm":     "${Show(nifta.fm)}", """ +
                    s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""
      RemoteBox.remoteCall("ifta", fmInfo, showProducts)
    } catch {
      case e:Throwable =>
        throw new RuntimeException("Not possible to calculate IFTA products: \n" + e.getMessage)
    }
  }

  private def showProducts(data:String):Unit = {
    deleteProducts()
    val solutions = DSL.parseProducts(data)
    solutions.map(mkSolButton)
  }

  private def deleteProducts(): Unit = {
    solutionsBox.html("")
  }

  private def mkSolButton(sol:Set[String]):Unit = {
    val text = sol.mkString(",")
    val b = solutionsBox.append("button").text(
      if (text == "") "⊥" else text)
    b.on("click",{(e:EventTarget, a:Int, b:UndefOr[Int]) => {
      iftaAut.showFs(sol)
    }}:b.DatumFunction[Unit])
  }
}
