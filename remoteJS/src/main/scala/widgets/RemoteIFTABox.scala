package widgets

import common.backend.{CCToFamily, NReoIFTA}
import common.widgets.Ifta.IFTABox
import common.widgets.{Box, GraphBox, OutputArea}
import ifta.{DSL, Feat, IFTA, NIFTA}
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


class RemoteIFTABox(dependency:Box[CoreConnector], iftaAutBox:IFTABox,circuitBox:GraphBox, errorBox:OutputArea)
  extends Box[IftaAutomata]("IFTA Analysis",List(dependency,iftaAutBox,circuitBox)){

  private var solutionsBox: Block = _
  private var iftaAut:IftaAutomata= _
  private var iftaAutSimple:IftaAutomata = _

  override def get: IftaAutomata = iftaAut

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

    dom.document.getElementById("IFTA Analysis").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
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
//      var nifta = NIFTA(Automata[IftaAutomata](dependency.get).nifta)
      iftaAut = Automata.toAutWithRedundandy[IftaAutomata](dependency.get)._1
//      iftaAutSimple = Automata[IftaAutomata](dependency.get)
      var nifta:NIFTA = NIFTA(iftaAut.nifta)
      var fmInfo =  s"""{ "fm":     "${Show(nifta.fm)}", """ +
                    s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""
//      var niftaSimple = NIFTA(iftaAutSimple.nifta)
//      var fmInfoSimple = s"""{ "fm":     "${Show(niftaSimple.fm)}", """ +
//        s"""  "feats":  "${niftaSimple.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""
      RemoteBox.remoteCall("ifta", fmInfo, showProducts)
//      RemoteBox.remoteCall("ifta",fmInfoSimple,showProducts)
    } catch {
      case e:Throwable =>
        errorBox.error(e.getMessage)
      //throw new RuntimeException("Not possible to calculate IFTA products: \n" + e.getMessage)
    }
  }

  private def showProducts(data:String):Unit = {
    deleteProducts()
    val solutions = DSL.parseProducts(data)
    // # of features
    solutionsBox.append("p")
      .append("strong")
      .text(s"Number of features: ${iftaAut.getFeats.size}\n")
    solutionsBox.append("ul")
      .attr("style","margin-bottom: 20pt;")
      .append("li")
      .text(s"${iftaAut.getFeats.map(Show(_)).mkString(",")}")
    // fm
    solutionsBox.append("p")
      .append("strong")
      .append("strong")
      .text("Feature model:")
    val list = solutionsBox.append("ul")
    list.attr("style","margin-bottom: 20pt;")
    list.append("li")
      .text(s"${Show(iftaAut.getFm)}")
    // solutions
    solutionsBox.append("p")
      .append("strong")
      .text(s"Solutions: ${solutions.size}")
    solutions.map(mkSolButton)
  }

  private def deleteProducts(): Unit = {
    solutionsBox.html("")
  }

  private def mkSolButton(sol:Set[String]):Unit = {
//    val renamedSols = sol.mkString(",")
    val renamedSols = sol.map(ft => Feat(ft)).map(ft => Show(iftaAut.getRenamedFe(ft))).mkString(",")
    val b = solutionsBox.append("button").text(
      if (renamedSols == "") "⊥" else renamedSols)
    b.on("click",{(e:EventTarget, a:Int, b:UndefOr[Int]) => {
      iftaAutBox.showFs(sol)
      circuitBox.showFs(sol)
    }}:b.DatumFunction[Unit])
  }
}
