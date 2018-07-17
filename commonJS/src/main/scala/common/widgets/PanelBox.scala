package common.widgets

import org.scalajs.dom
import org.singlespaced.d3js.Selection


//panel boxes are the abstract entities which contain each panel displayed on the website
abstract class PanelBox[A](title: String, dependency: Option[PanelBox[_]]){
  type Block = Selection[dom.EventTarget]


  /**
    * Creates a collapsable pannel
    * */
  protected def panelBox(parent:Block
                       ,visible:Boolean=true
                       ) : Block = {

    var expander: Block = parent
    val wrap = parent.append("div").attr("class","panel-group")
      .append("div").attr("class","panel panel-default").attr("id",title)
    expander = wrap
      .append("div").attr("class", "panel-heading my-panel-heading")
      .append("h4").attr("class", "panel-title")
      .append("a").attr("data-toggle", "collapse")
      .attr("href", "#collapse-1" + title.hashCode)
      .attr("aria-expanded", visible.toString)
    if(!visible)
      expander.attr("class","collapsed")
    expander
      .text(title)
    val res = wrap
      .append("div").attr("id","collapse-1"+title.hashCode)
      .attr("class",if (visible) "panel-collapse collapse in" else "panel-collapse collapse")
      .attr("style",if (visible) "" else "height: 0px;")
      .attr("aria-expanded",visible.toString)
      .append("div").attr("class","panel-body my-panel-body")

    res
  }

  def isVisible: Boolean = {
    val es = dom.document.getElementsByClassName("collapsed")
    var foundId = false
    for (i <- 0 until es.length) {
      println(es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      //      println("### - "+es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      foundId = foundId || es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value == title
    }

    //    println("### - "+es.length)
    //    println("### - "+es.item(0).localName)
    //    println("### - "+es.item(0).parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)

    //    val res = expander.attr("aria-expander") == "true"
    //    println("--- "+expander.html().render)
    //    println("--- "+expander.classed("collapsed"))
    //    println("--- "+expander.attr("aria-expander"))
    //    println("$$$ "+ (!foundId))
    !foundId
  }

  def get: A

  def init(div: Block): Unit

  def update: Unit
}


object PanelBox{
  type Block = Selection[dom.EventTarget]
  var width = 700
  var height = 400

  def appendSvg(div: Block,name: String): Block = {
    val svg = div.append("svg")
      //      .attr("width", "900")
      //      .attr("height", "600")
      //      .style("border", "black")
      //      .style("border-width", "thin")
      //      .style("border-style", "solid")
      .attr("style","margin: auto;")
      .attr("viewBox",s"0 0 $width $height")
      .attr("preserveAspectRatio","xMinYMin meet")
      .attr("id",name)
      .style("margin", "auto")

    svg.append("g")
      .attr("class", "links"+name)

    svg.append("g")
      .attr("class", "nodes"+name)

    svg.append("g")
      .attr("class", "labels"+name)

    svg.append("g")
      .attr("class", "paths"+name)

    //inserting regular arrow at the end
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowout"+name)
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",20.5)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",7)
      .attr("markerHeight",7)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L 10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    //arrowhead inverted for sync drains
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowin"+name)
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",20.5)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",7)
      .attr("markerHeight",7)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 10,-5 L 0 ,0 L 10,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowout"+name)
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-15)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",10)
      .attr("markerHeight",10)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L -10 ,0 L 0,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","startarrowin"+name)
      .attr("viewBox","-10 -10 16 16")
      .attr("refX",-22)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",10)
      .attr("markerHeight",10)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M -10,-5 L 0 ,0 L -10,5")
      .attr("fill", "#000")
      .style("stroke","none");

    svg.append("defs")
      .append("marker")
      .attr("id","boxmarker"+name)
      .attr("viewBox","0 0 60 30")
      .attr("refX","30")
      .attr("refY","15")
      .attr("markerUnits","strokeWidth")
      .attr("markerWidth","18")
      .attr("markerHeight","9")
      .attr("stroke","black")
      .attr("stroke-width","6")
      .attr("fill","white")
      .attr("orient","auto")
      .append("rect")
      .attr("x","0")
      .attr("y","0")
      .attr("width","60")
      .attr("height","30")

    svg.append("defs")
      .append("marker")
      .attr("id","boxfullmarker"+name)
      .attr("viewBox","0 0 60 30")
      .attr("refX","30")
      .attr("refY","15")
      .attr("markerUnits","strokeWidth")
      .attr("markerWidth","18")
      .attr("markerHeight","9")
      .attr("stroke","black")
      .attr("stroke-width","6")
      .attr("fill","black")
      .attr("orient","auto")
      .append("rect")
      .attr("x","0")
      .attr("y","0")
      .attr("width","60")
      .attr("height","30")

    svg
  }

}