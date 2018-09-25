package widgets

import common.widgets.{ErrorArea, Box}
import json.Loader
import org.scalajs.dom.raw.{Event, MessageEvent, WebSocket}
import preo.ast.CoreConnector
import preo.frontend.Show

class RemoteInstanceBox(reload: => Unit, dependency: Box[String], errors: ErrorArea)
  extends Box[CoreConnector]("Type", List(dependency))
  {
    private var typeInfo: Block = _
    private var instanceInfo: Block = _
    private var ccon: CoreConnector = _

    var id: Long = 0


    /**
      * Creates a collapsable pannel
      **/
    protected def panelBox(title: String, parent: Block, visible: Boolean): Block = {
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

    override def get: CoreConnector = ccon

    override def init(div: Block, visible: Boolean): Unit = {
      typeInfo = panelBox(div, visible).append("div")
        .attr("id", "typeBox")

      instanceInfo = panelBox("Concrete instance",div, true).append("div")
      //      .attr("id", "instanceBox")
    }

    override def update: Unit = {
      val socket = new WebSocket("ws://localhost:9000/message")

      socket.onmessage = { e: MessageEvent => {process(e.data.toString); socket.close()}}// process(e.data.toString, typeInfo, instanceInfo, svg, svgAut, errors) }

      socket.addEventListener("open", (e: Event) => {
        socket.send(dependency.get)
      })
    }


    def process(receivedData: String): Unit = {
      typeInfo.text("")
      instanceInfo.text("")

      val result = Loader(receivedData)
      println(result)

      //    println(result)
      result match {
        case Right(message) => {
          errors.error(message)
        }
        case Left((typ, reducTyp, con, ide)) => {
          typeInfo.append("p")
            .text(typ)
          instanceInfo.append("p")
            .text(Show(con) + ":\n  " +
              reducTyp)
          ccon = con
          this.id = ide
          reload
        }
      }
    }
  }
