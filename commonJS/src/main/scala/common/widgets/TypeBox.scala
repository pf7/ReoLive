package common.widgets

import preo.DSL
import preo.ast.{BVal, Connector}
import preo.common.TypeCheckException
import preo.frontend.Show


//todo: this should be local to localJS
class TypeBox(dependency: PanelBox[String], errors: ErrorBox) extends PanelBox[Connector]("Type", Some(dependency)){

  var con: Connector = _
  var typeInfo: Block = _


  override def get: Connector = con

  override def init(div: Block): Unit = {
    typeInfo = panelBox(div, true).append("div")
      .attr("id", "typeBox")
  }

  override def update: Unit = {

    typeInfo.text("")
    DSL.parseWithError(dependency.get) match {
      case preo.lang.Parser.Success(result, _) =>
        try {
          val typ = DSL.unsafeCheckVerbose(result)
          val (_, rest) = DSL.unsafeTypeOf(result)
          typeInfo.append("p")
            .text(Show(typ))
          if (rest != BVal(true))
            errors.warning(s"Warning: did not check if ${Show(rest)}.")
          con = result
        }
        catch {
          // type error
          case e: TypeCheckException =>
            errors.error(/*Show(result)+ */ "Type error: " + e.getMessage)
        }
      case preo.lang.Parser.Failure(msg,_) =>
        errors.error("Parser failure: " + msg)
        //        instanceInfo.append("p").text("-")
      case preo.lang.Parser.Error(msg,_) =>
        errors.error("Parser error: " + msg)
        //        instanceInfo.append("p").text("-")
    }
  }
}
