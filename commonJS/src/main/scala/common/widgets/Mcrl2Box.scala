package common.widgets

import java.util.Base64

import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, XMLHttpRequest, html}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.Model

class Mcrl2Box(dependency: Box[CoreConnector], errorBox: OutputArea)
    extends Box[Model]("mCRL2 of the instance", List(dependency))  {
  private var box: Block = _
  private var model: Model = _

  override def get: Model = model

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible,buttons=List(Left("&dArr;")-> (()=>download())))
      .append("div")
      .attr("id", "mcrl2Box")


    dom.document.getElementById("mCRL2 of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { _: MouseEvent => if (!isVisible) produceMcrl2() else deleteMcrl2()}
  }

  private def download(): Unit = {
//    <a href="data:application/octet-stream;charset=utf-16le;base64,//5mAG8AbwAgAGIAYQByAAoA">text file</a>
    val enc = Base64.getEncoder.encode(get.toString.getBytes()).map(_.toChar).mkString
    val filename = "model.mcrl2"
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
  override def update(): Unit = if(isVisible) produceMcrl2()

  private def produceMcrl2(): Unit = {
    model = Model(dependency.get)
    box.html(model.webString)
  }

  private def deleteMcrl2(): Unit = {
    box.html("")
  }

}
