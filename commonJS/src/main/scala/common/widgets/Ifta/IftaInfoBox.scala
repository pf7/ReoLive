package common.widgets.Ifta

import common.widgets.{Box, OutputArea}
import ifta.IFTA
import ifta.backend.{IftaAutomata, Show}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.Automata

/**
  * Created by guillecledou on 18/03/2019
  */


class IftaInfoBox(dependency:Box[CoreConnector], errorBox:OutputArea)
  extends Box[IFTA]("IFTA Analysis",List(dependency)) {

  private var box: Block = _
  private var ifta: IFTA = _


  override def get: IFTA = ifta

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible).append("div")
      .attr("id", "iftaAnalysisBox")
    dom.document.getElementById("IFTA Analysis").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) showInfo() else deleteInfo() }
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if (isVisible) showInfo()


  private def showInfo() = {
    deleteInfo()
    try{
      var iftaAut = Automata.toAutWithRedundandy[IftaAutomata](dependency.get)
      ifta = iftaAut.ifta

//      // clocks
//      box.append("p")
//        .append("strong")
//        .text(s"Clocks: ${ifta.clocks.size}\n")
//      if (ifta.clocks.nonEmpty) {
//        box.append("ul")
//          .attr("style", "margin-bottom: 20pt;")
//          .append("li")
//          .text(s"${ifta.clocks.mkString(",")}")
//      }
      // # of features
      box.append("p")
        .append("strong")
        .text(s"Number of features: ${ifta.feats.size}\n")
      box.append("ul")
        .attr("style","margin-bottom: 20pt;")
        .append("li")
        .text(s"${iftaAut.getFeats.map(Show(_)).mkString(",")}")
      // fm
      box.append("p")
        .append("strong")
        .text("Feature model:")
      val list = box.append("ul")
      list.attr("style","margin-bottom: 20pt;")
      list.append("li")
        .text(s"${Show(iftaAut.getFm)}")
    }
    catch Box.checkExceptions(errorBox)
  }

  private def deleteInfo() =
    box.text("")

}
