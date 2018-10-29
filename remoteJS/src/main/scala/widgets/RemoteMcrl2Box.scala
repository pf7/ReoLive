package widgets

import common.widgets.{Box, OutputArea}
import org.scalajs.dom
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.{EventTarget, MouseEvent, html}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.Model

import scala.scalajs.js.UndefOr



class RemoteModelBox(connector: Box[CoreConnector], errorBox: OutputArea)
    extends Box[Model]("mCRL2 of the instance", List(connector)){

  var id: Long = 0
  private var box: Block = _
  private var model: Model = _

  override def get: Model = model

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, List("padding-right"->"90pt"),
      buttons=List(Left("&dArr;")-> (()=>download(s"/model/$id"))
                  ,Left("LPS")   -> (()=>download(s"/lps/$id"))
                  ,Left("LTS")   -> (()=>download(s"/lts/$id"))
//                  ,Left("MA")   -> (()=> debugNames)
                  ))
      .append("div")
      .attr("id", "mcrl2Box")


    dom.document.getElementById("mCRL2 of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) produceMcrl2() else deleteMcrl2()}
  }

//  private def debugNames(): Unit = {
//    errorBox.clear()
//    errorBox.warning(model.getMultiActionsMap
//      .map(kv => kv._1+":"+kv._2.map("\n - "+_).mkString(""))
//      .mkString("\n"))
//  }

  private def download(url: String): Unit = {
    val x = new XMLHttpRequest()
    x.open("GET", url, async = true)
    x.onload = e => {
      if(x.status == 200){
        scalajs.js.eval(
          s"""
            let a = document.createElement("a");
            a.style = "display: none";
            document.body.appendChild(a);
            a.href = "$url";
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
    model = Model(connector.get)
    box.html(model.webString)
  }

  private def deleteMcrl2(): Unit = {
    box.html("")
  }
}
