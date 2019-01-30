package common.widgets

class PreoBox (globalReload: =>Unit, export: => Unit,default: String, outputBox: OutputArea)
  extends Box[String]("Reo program", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "PreoInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh")-> (()=>reload,"Load the connector (shift-enter)"),
      Left("URL")-> (()=>export,"Generate link with the current connector"))

  override protected val codemirror: String = "preo"

  override def reload(): Unit = {
    update() // load content of the window to an internal variable
    outputBox.clear() // no error yet
    globalReload
  }



}