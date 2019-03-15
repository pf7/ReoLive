package common.widgets.Ifta

import common.widgets.{Box, OutputArea}
import ifta.NIFTA
import preo.ast.CoreConnector

/**
  * Created by guillecledou on 03/01/2019
  */


class IFTAGraphBox (dependency:Box[CoreConnector], errorBox:OutputArea)
  extends Box[NIFTA]("NIFTA graph of the instance",List(dependency)){

  private var nifta: NIFTA = _
  private var box: Block = _

  private val widthCircRatio = 7
  private val heightCircRatio = 3
  private val densityCirc = 0.5 // nodes per 100x100 px

  override def get: NIFTA = ???

  /**
    * Executed once at creation time, to append the content to the inside of this box
    *
    * @param div     Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  override def init(div:  Block, visible: Boolean): Unit = ???

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
override def update(): Unit = ???
}
