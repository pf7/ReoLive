package modules
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Selection
import preo.DSL
import preo.ast.BVal
import preo.common.TypeCheckException
import preo.frontend.Show
import reolive.{State, Structure}
import scalatags.JsDom.all.s

class Type extends Module{

  private var typeArea: Selection[EventTarget] = null


  override def spawn(block: Selection[EventTarget]): Unit =
    typeArea = Structure.panelBox(block,"Type").append("div")
      .attr("id", "typeBox")

  override def update_local: Option[String] = {
      val typ = DSL.unsafeCheckVerbose(State.connector)
      val (_,rest) = DSL.unsafeTypeOf(State.connector)
      typeArea.append("p")
        .text(Show(typ))
      if (rest != BVal(true))
        Some(s"Warning: did not check if ${Show(rest)}.")
      else
        None
    }

  override def update_remote: Unit = ???

  def clear : Unit = typeArea.text("")
}
