package widgets

import common.widgets.{ErrorArea, Box}
import hprog.ast.Prog
import hprog.DSL
import hprog.frontend.Show


//todo: this should be local to localJS
class HProgBox(dependency: Box[String], errorBox: ErrorArea) extends Box[Prog]("Parsed program", Some(dependency)){

  // state
  var prog: Prog= _
  var block: Block = _


  override def get: Prog = prog

  override def init(div: Block, visible: Boolean): Unit = {
    block = panelBox(div, visible).append("div")
      .attr("id", "HProgBox")
  }

  override def update: Unit = {

    block.text("")
    DSL.parseWithError(dependency.get) match {
      case hprog.lang.Parser.Success(result, _) =>
          block //.append("p")
            .html(Show(result).replace("\n"," <br>\n"))
          prog = result
      case hprog.lang.Parser.Failure(msg,_) =>
        errorBox.error("Parser failure: " + msg)
      //        instanceInfo.append("p").text("-")
      case hprog.lang.Parser.Error(msg,_) =>
        errorBox.error("Parser error: " + msg)
      //        instanceInfo.append("p").text("-")
    }
  }
}
