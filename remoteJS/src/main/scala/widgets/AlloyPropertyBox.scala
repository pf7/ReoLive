package widgets

import common.widgets.{Box, CodeBox, OutputArea}

class AlloyPropertyBox (globalReload: =>Unit, checkRL: =>Unit, nextRL: =>Unit, export: => Unit,default: String, outputBox: OutputArea)
  extends Box[String]("Reo Alloy property checker", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "AlloyPropertiesInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Left("check")-> (()=>check,"Check property"),
      Left("next")-> (()=>next,"Show next counter-example")
    )

  override protected val codemirror: String = "preo"

  override def reload(): Unit = {
    update() // load content of the window to an internal variable
    outputBox.clear() // no error yet
    globalReload
  }

  def check(): Unit ={
    update()
    outputBox.clear()
    checkRL
  }

  def next(): Unit ={
    update()
    outputBox.clear()
    nextRL
  }

}
