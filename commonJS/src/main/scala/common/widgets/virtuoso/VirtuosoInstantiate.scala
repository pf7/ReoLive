package common.widgets.virtuoso

import common.widgets.{Box, OutputArea}
import common.widgets.virtuoso.VirtuosoParser.Result
import preo.DSL
import preo.ast.{BVal, Connector, CoreConnector}
import preo.frontend.{Eval, Show, Simplify}

class VirtuosoInstantiate(code: Box[String], errorBox: OutputArea)
    extends Box[CoreConnector]("Concrete instance", List(code)){

  private var ccon: CoreConnector = _

  override def get: CoreConnector = ccon

  override def init(div: Block, visible: Boolean): Unit = {}

  override def update(): Unit = try {
     parseAndTypeCheck(code.get) match {
       case Left(value) =>  errorBox.error(value)
       case Right(c) => instantiate(c) match {
         case Left(value) => errorBox.error(value)
         case Right(value) => ccon = value
       }
     }
  }
      catch {
        case e:Throwable =>
          Box.checkExceptions(errorBox).apply(e)
      }


  def parseAndTypeCheck(s:String): Result[Connector] =
    VirtuosoParser.parse(s) match {
      case Right(result) =>
        val _ = DSL.unsafeCheckVerbose(result)
        val (_, rest) = DSL.unsafeTypeOf(result)
        if (rest != BVal(true))
          errorBox.warning(s"Warning: did not check if ${Show(rest)}.")
        Right(result)
      case Left(msg) =>
        Left("Parser error: " + msg)
    }


  def instantiate(c:Connector): Result[CoreConnector] = {
    Eval.unsafeInstantiate(c) match {
      case Some(reduc) =>
        // GOT A TYPE
        Right(Eval.unsafeReduce(reduc))
      case _ =>
        // Failed to simplify
        Left("Failed to reduce connector: " + Show(Simplify.unsafe(c)))
    }
  }


}
