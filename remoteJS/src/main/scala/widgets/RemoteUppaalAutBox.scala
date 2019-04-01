package widgets

import java.util.Base64

import common.widgets.{Box, OutputArea}
import ifta.analyse.IFTA2FTA
import ifta.{DSL, NIFTA}
import ifta.backend.{IftaAutomata, Show, Uppaal}
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.backend.Automata


/**
  * Created by guillerminacledou on 2019-03-30
  */


class RemoteUppaalAutBox(connector:Box[CoreConnector], errorBox:OutputArea)
  extends Box[String]("Uppaal TA model",List(connector)) {

  private var box:Block = _
  private var uppaalAut:String = _
  private var iftaAut:IftaAutomata = _

  override def get: String = uppaalAut

  //todo: convert ifta with fancy names firts (uppaal doesn't support identifiers starting with number.
  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List(Left("&dArr;")-> (()=>download(), "Download IFTA model as a TA with variability in Uppaal")))
      .append("div")
      .attr("id", "uppaalAutBox")
    dom.document.getElementById("Uppaal TA model")
      .firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if (!isVisible) solveModel() else deleteModel()}
  }

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  override def update(): Unit = if (isVisible) solveModel()

  /** Solve feature model to create uppaal model */
  def solveModel():Unit = {
    deleteModel()
    iftaAut = Automata[IftaAutomata](connector.get)

    var nifta:NIFTA = NIFTA(iftaAut.nifta)
    var fmInfo =  s"""{ "fm":     "${Show(nifta.fm)}", """ +
      s"""  "feats":  "${nifta.iFTAs.flatMap(i => i.feats).mkString("(",",",")")}" }"""

    RemoteBox.remoteCall("ifta", fmInfo, showModel)
  }

  /** show uppaal model for ifta flatten in a timed automata */
  def showModel(data:String):Unit = {
    val solutions = DSL.parseProducts(data)
    uppaalAut = DSL.toUppaal(iftaAut.ifta,solutions)
    println("uppaaal model: \n" + uppaalAut)
    box.append("textarea")
        .attr("id","uppaalAutModel")
        .style("white-space","pre-wrap")
        .text(uppaalAut)

    val codemirrorJS = scalajs.js.Dynamic.global.CodeMirror
    val lit = scalajs.js.Dynamic.literal(
      lineNumbers = true, matchBrackets = true, lineWrapping = true,
      readOnly = true, theme = "default", cursorBlinkRate = -1, mode="application/xml")
    codemirrorJS.fromTextArea(dom.document.getElementById("uppaalAutModel"),lit)
  }
  // todo: perhaps this should be a reusable method, e.g. in box, becuase many boxes use this.
  private def download(): Unit = {
    val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
    val filename = "uppaalTA.xml"
    val url= "data:application/octet-stream;charset=utf-16le;base64,"+enc
    //
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = e => {
      if(x.status == 200){
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
            a.download="$filename";
            a.text = "hidden link";
            //programatically click the link to trigger the download
            a.click();
            //release the reference to the file by revoking the Object URL
            window.URL.revokeObjectURL("$url");
          """
        )
      }
      else if(x.status == 404){
        errorBox.error(x.responseText)
      }
    }
    x.send()
  }

  def deleteModel():Unit =
    box.html("")

}
