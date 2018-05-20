package reolive

import common.ParserException
import org.scalajs.dom.html
import preo.DSL
import preo.ast.{BVal, CoreConnector}
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.{Eval, Show, Simplify}

import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.JSExportTopLevel
import modules.{Error, Input, Instance, Type}
import org.scalajs.dom

object LocalReo{

  val input = new Input()
  val error = new Error()
  val ctype = new Type()
  val cinstance = new Instance()

  @JSExportTopLevel("reolive.LocalReo.main")
  def main(content: html.Div) = {
    //appends the divs to create the site structure
    val (colDiv1, svgDiv) = Structure.appendDivs(content)

    // add InputArea


    input.spawn(colDiv1)


    input.inputArea.onkeydown = {(e: dom.KeyboardEvent) =>
      if(e.keyCode == 13 && e.shiftKey){e.preventDefault() ; update()}
      else ()
    }

    error.spawn(colDiv1)

    ctype.spawn(colDiv1)

    cinstance.spawn(colDiv1)


  }

  private def update(): Unit = {
    ctype.clear

    try {
      input.update_local
      println(State.connector)
      val type_warnings = ctype.update_local
      type_warnings match {
        case Some(s) => error.warning(s)
        case None =>
      }
      val instance_warnings = cinstance.update_local
      instance_warnings match{
        case Some(s) => error.warning(s)
        case None =>
      }


    }
    catch {
      case e: ParserException =>
        error.error("Parser failure: " + e.getMessage)
      // type error
      case e: TypeCheckException =>
        error.error("Type error: " + e.getMessage)
      case e: GenerationException =>
        error.warning("Generation failed: " + e.getMessage)
      case e: JavaScriptException =>
        error.error("JavaScript error : "+e+" - "+e.getClass)
      //            instanceInfo.append("p").text("-")
      case e: Exception =>
        error.error("Error : "+e.getMessage)
    }
  }

}
