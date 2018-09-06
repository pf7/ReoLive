package widgets

import common.widgets.{ErrorBox, PanelBox}
import org.scalajs.dom
import org.scalajs.dom.raw.XMLHttpRequest
import org.scalajs.dom.{EventTarget, MouseEvent, html}
import preo.ast.CoreConnector
import preo.frontend.mcrl2.Model

import scala.scalajs.js.UndefOr



class RemoteModelBox(dependency: PanelBox[CoreConnector], errorBox: ErrorBox) extends PanelBox[Model]("mCRL2 of the instance", Some(dependency)){

  var id: Long = 0
  private var box: Block = _
  private var model: Model = _

  override def get: Model = model

  override def init(div: Block, visible: Boolean): Unit = {
    box = panelBox(div, visible, List("padding-right"->"90pt")).append("div")
      .attr("id", "mcrl2Box")

    val ltsButton = wrap
      .select("div")
      .append("button").attr("class", "btn btn-default btn-sm")
      .style("float","right")
      .style("margin-top","-15pt")
      .style("max-height","19pt")
      .style("margin-left","2pt")

    ltsButton.append("span").html("LTS")
    ltsButton.on("click", {(e: EventTarget, a: Int, b:UndefOr[Int])=> { download(s"/lts/$id")}})

    val lpsButton = wrap
      .select("div")
      .append("button").attr("class", "btn btn-default btn-sm")
        .style("float","right")
        .style("margin-top","-15pt")
        .style("max-height","19pt")
        .style("margin-left","2pt")

    lpsButton.append("span").html("LPS")
    lpsButton.on("click", {(e: EventTarget, a: Int, b:UndefOr[Int])=> { download(s"/lps/$id")}})

    val modelButton = wrap
      .select("div")
      .append("button").attr("class", "btn btn-default btn-sm")
      .style("float","right")
      .style("margin-top","-15pt")
      .style("max-height","19pt")
      .style("margin-left","2pt")


    modelButton.append("span").html("&dArr;")
    modelButton.on("click", {(e: EventTarget, a: Int, b:UndefOr[Int])=> { download(s"/model/${id}")}})



    dom.document.getElementById("mCRL2 of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = { e: MouseEvent => if (!isVisible) produceMcrl2 else deleteModel}
  }

  private def download(url: String) = {
    val x = new XMLHttpRequest()
    x.open("GET", url, true)
    x.onload = (e) => {
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
            window.URL.revokeObjectURL(url);
          """
        )
      }
      else if(x.status == 404){
        errorBox.error(x.responseText)
      }
    }
    x.send()

  }
  override def update: Unit = if(isVisible) produceMcrl2

  private def produceMcrl2: Unit = {
    model = Model(dependency.get)
    box.html(model.webString)
  }

  private def deleteModel: Unit = {
    box.html("")
  }
}
