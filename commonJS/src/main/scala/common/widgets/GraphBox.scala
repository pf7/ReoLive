package common.widgets

import common.frontend.GraphsToJS
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import org.singlespaced.d3js.Selection
import preo.ast.CoreConnector
import preo.backend.Circuit
import preo.backend.Network.Mirrors
import preo.frontend.Show

import scala.util.Try

class GraphBox(dependency: Box[CoreConnector], errorBox: OutputArea, path: String=".")
    extends Box[Circuit]("Circuit of the instance", List(dependency)) {
  var graph: Circuit = _
  var box: Block = _
  override def get: Circuit = graph

  protected val widthCircRatio = 7
  protected val heightCircRatio = 3
  protected val densityCirc = 0.5 // nodes per 100x100 px


  override def init(div: Block, visible: Boolean): Unit = {
    box = GraphBox.appendSvg(super.panelBox(div,visible,
      buttons = List(
        Left("&dArr;")-> (() => saveSvg(),"Download image as SVG")
      )),"circuit", path=path)
    dom.document.getElementById("Circuit of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawGraph() else deleteDrawing()}
  }

  override def update(): Unit = if(isVisible) {
    deleteDrawing()
    drawGraph()
  }


  protected def drawGraph(): Unit = try{
    graph = Circuit(dependency.get,true)
    val size = graph.nodes.size
    val factor = Math.sqrt(size * 10000 / (densityCirc * widthCircRatio * heightCircRatio))
    val width = (widthCircRatio * factor).toInt
    val height = (heightCircRatio * factor).toInt
    box.attr("viewBox", s"00 00 $width $height")
    //println("Drawing graph - source: "+dependency.get)
    //println("Drawing graph - produced: "+ graph)
//    toJs(graph)
    scalajs.js.eval(GraphsToJS(graph))
  }
  catch Box.checkExceptions(errorBox)



//  protected def toJs(g:Graph):Unit = scalajs.js.eval(GraphsToJS(g))

  protected def deleteDrawing(): Unit = {
    box.selectAll("g").html("")
  }

  protected def saveSvg(): Unit = {
    scalajs.js.eval(
      """svgEl = document.getElementById("circuit");
        |name = "circuit.svg";
        |
        |svgEl.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        |var svgData = svgEl.outerHTML;
        |
        |// Firefox, Safari root NS issue fix
        |svgData = svgData.replace('xlink=', 'xmlns:xlink=');
        |// Safari xlink NS issue fix
        |//svgData = svgData.replace(/NS\d+:href/gi, 'xlink:href');
        |svgData = svgData.replace(/NS\d+:href/gi, 'href');
        |// drop "stroke-dasharray: 1px, 0px;"
        |svgData = svgData.replace(/stroke-dasharray: 1px, 0px;/gi, '');
        |
        |var preface = '<?xml version="1.0" standalone="no"?>\r\n';
        |var svgBlob = new Blob([preface, svgData], {type:"image/svg+xml;charset=utf-8"});
        |var svgUrl = URL.createObjectURL(svgBlob);
        |var downloadLink = document.createElement("a");
        |downloadLink.href = svgUrl;
        |downloadLink.download = name;
        |document.body.appendChild(downloadLink);
        |downloadLink.click();
        |document.body.removeChild(downloadLink);
      """.stripMargin)

//    //val svgEl = dom.document.getElementById("circuit")
//    val svgEl = box
//    val name = "circuit.svg"
//
//    svgEl.attr("xmlns", "http://www.w3.org/2000/svg")
//    var svgData = svgEl.html()
//
//    // Firefox, Safari root NS issue fix
//    svgData = svgData.replace("xlink=", "xmlns:xlink=")
//    // Safari xlink NS issue fix
//    //svgData = svgData.replace(/NS\d+:href/gi, 'xlink:href');
//    svgData = svgData.replaceAll("NS\\d+:href", "href")
//    // drop "stroke-dasharray: 1px, 0px;"
//    svgData = svgData.replace("stroke-dasharray: 1px, 0px;", "")
//
//    val preface = """<?xml version="1.0" standalone="no"?>\r\n"""
//    val svgBlob = scalajs.js.Dynamic.newInstance(scalajs.js.Dynamic.global.Blob)(
//      List(preface,svgData), // does not type check... should be "[preface, svgData]"
//      Map("type" -> "image/svg+xml;charset=utf-8"))
//    val svgUrl = scalajs.js.Dynamic.global.URL.createObjectURL(svgBlob)
//    val downloadLink = dom.document.createElement("a")
//    downloadLink.setAttribute("href",svgUrl.asInstanceOf[String])
//    downloadLink.setAttribute("download",name)
//    dom.document.body.appendChild(downloadLink)
//    scalajs.js.Dynamic.global.downloadLink.click()
//    dom.document.body.removeChild(downloadLink)
  }

  def showFs(fs:Set[String]) = if (isVisible) {
    val showCircuit =
      s"""
         |var fs = new Set(${fs.map(s=> s""""$s"""").mkString("[",",","]")});
         |d3.select(".linkscircuit")
         |  .selectAll("polyline")
         |  .style("opacity", function(d) {
         |    var srcPorts = d.source.ports;
         |    var trgPorts = d.target.ports;
         |    var srcIntersect = new Set(srcPorts.filter(x => fs.has(x)));
         |    var trgIntersect = new Set(trgPorts.filter(x => fs.has(x)));
         |    return ((srcIntersect.size > 0) && (trgIntersect.size > 0)) ? "1" : "0.1"
         |  });
         |
       """.stripMargin

    scalajs.js.eval(showCircuit)
  }


    }

object GraphBox {
  type Block = Selection[dom.EventTarget]

  private var width = 700
  private var height = 400

  def appendSvg(div: Block,name: String, path:String = "."): Block = {
    val svg = div.append("svg")
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
      .style("stroke","none")

    //inserting light arrow at the end for feature selection not satisfied
    svg.append("defs")
      .append("marker")
      .attr("id","endarrowout"+name+"light")
      .attr("viewBox","-0 -5 10 10")
      .attr("refX",20.5)
      .attr("refY",0)
      .attr("orient","auto")
      .attr("markerWidth",7)
      .attr("markerHeight",7)
      .attr("xoverflow","visible")
      .append("svg:path")
      .attr("d", "M 0,-5 L 10 ,0 L 0,5")
      .attr("fill", "#cccccc")
      .style("stroke","none")

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
      .style("stroke","none")

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
      .style("stroke","none")

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
      .style("stroke","none")

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

    svg.append("defs")
      .append("marker")
      .attr("id","timermarker"+name)
      .attr("refX","6")
      .attr("refY","6")
      .attr("markerUnits","strokeWidth")
      .attr("markerWidth","24")
      .attr("markerHeight","24")
      .attr("stroke","black")
      .attr("stroke-width","1")
      .attr("fill","white")
//      .attr("orient","auto")
      .append("circle")
      .attr("cx","6")
      .attr("cy","6")
      .attr("r","5")
      .attr("fill","white")

//    svg.append("defs")
//      .append("pattern")
//      .attr("id", "xorpattern")
//      .attr("patternUnits", "userSpaceOnUse")
//      .attr("width", 10)
//      .attr("height", 10)
//      .append("image")
//        .attr("xlink:href", s"$path/svg/x.svg")
//        .attr("width", 10)
//        .attr("height", 10);


    svg
  }
}
