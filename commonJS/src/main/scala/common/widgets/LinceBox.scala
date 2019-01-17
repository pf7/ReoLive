package common.widgets

class LinceBox (globalReload: =>Unit, default: String, outputBox: OutputArea)
  extends Box[String]("Hybrid Program", Nil) with CodeBox {

  override protected var input: String = default
  override protected val boxId: String = "linceInputArea"
  override protected val buttons: List[(Either[String, String], (() => Unit, String))] =
    List(
      Right("glyphicon glyphicon-refresh") -> (() => reload, "Load the Lince program (shift-enter)")
//      Left("MA") -> (() => debugNames, "Map actions in the formula to sets of actions in the mCRL2 specification")
    )

  override protected val codemirror: String = "lince"

  override def reload(): Unit = {
    update()
    outputBox.clear()
    globalReload
  }

}
