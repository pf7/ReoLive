package widgets

import common.backend.{CCToFamily, NReoIFTA}
import common.widgets.{Box, OutputArea}
import ifta.IFTA
import ifta.backend.Show
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.MouseEvent
import preo.ast.CoreConnector

/**
  * Created by guille on 16/01/2019
  */


class RemoteIFTABox(dependency:Box[CoreConnector], errorBox:OutputArea)
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
      .onclick = { e : MouseEvent => if (!isVisible) process else deleteProducts}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if (isVisible) process
//  {
//    solutionsBox.text("")
//    try {
//      var rifta = CCToFamily.toRifta(dependency.get)
//      var fm = rifta.getFm
//      //getIFTA(hideIntenal = true)
//      var fmInfo = Show(fm)
//      //    if(isVisible) showProducts()
//      RemoteBox.remoteCall("ifta", fmInfo, process)
//    } catch {
//      case e:Throwable =>
//        throw new RuntimeException("not possible to calculate IFTA products: \n" + e.getMessage)
//    }
//  }

  private def process():Unit ={
    try {
      var rifta = CCToFamily.toRifta(dependency.get)
      var fm = rifta.getFm
      //getIFTA(hideIntenal = true)
      var fmInfo = Show(fm)
      //    if(isVisible) showProducts()
      RemoteBox.remoteCall("ifta", fmInfo, showProducts)
    } catch {
      case e:Throwable =>
        throw new RuntimeException("not possible to calculate IFTA products: \n" + e.getMessage)
    }
  }

  private def showProducts(data:String):Unit = {
    solutionsBox.text("")
    solutionsBox.append("p").text(data)
  }

  private def deleteProducts(): Unit = {
    solutionsBox.html("")
  }
}
