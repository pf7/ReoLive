package services

import java.io.{File, PrintWriter}

import akka.actor._
import preo.DSL
import preo.ast.{Connector, CoreConnector}
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.{Eval, Show}
import MCRL2Bind._
import common.widgets.LogicBox
import play.api.libs.json.JsValue
import preo.frontend.mcrl2.{Formula, Model}
import preo.lang.{FormulaParser, ParserUtils}



object ModalActor {
  def props(out: ActorRef) = Props(new ModalActor(out))
}


class ModalActor(out: ActorRef) extends Actor{
  /**
    * Reacts to messages containing a JSON with a connector (string) and a modal formula (string),
    * produces a mcrl2 model and its LPS from the connector,
    * calls mcrl2 to verify the formula,
    * wraps each into a new JSON (via process),
    * and forwards the result to the "out" actor to generate an info (or error) box.
    */
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  /**
    * Get a request to view or model check a raw connector (string) and a raw formula (string)
    * @param msg
    * @return
    */
  private def process(msg: String): String = {
    val (raw_connector, raw_formula, operation) = JsonLoader.parse(msg)
    (raw_connector,raw_formula,operation) match {
      case (Some(c),Some(f),Some("check")) => modelCheck(c, f)
      case (Some(c),Some(f),Some("view")) => view(c, f)
      case (None,_,_) => error("Parser error: no connector found")
      case (_,None,_) => error("Parser error: no modal logic found")
      case _ => error("Unknown operation: " + operation)
    }
  }

  /**
    * * Get a request to model check a raw connector (string) and a raw formula (string)
    * * 1 - parse formula
    * * 2 - collect tree to hide/unhide
    * * 3 - parse connector
    * * 4 - generate mCRL2 model of connector
    * * 5 - override hides in mCRL2 model
    * * 6 - convert formula to mCRL2 formula (the model is needed)
    * * 7 - model check (6) against (5)
    * * @param msg
    * * @return
    *
    * @param conn
    * @param form
    * @return
    */
  private def modelCheck(raw_conn: String, raw_form: String): String = {
    ParserUtils.parseAndHide(raw_conn, raw_form) match {
      case Right((model, mcrl2form)) => modelCheck(model, mcrl2form)
      case Left(err) => error(err)
    }

  }

//  private def parseFormula(in:String): Either[String,Formula] = {
//    FormulaParser.parse(in) match {
//      case FormulaParser.Success(result, _) => Right(result)
//      case f: FormulaParser.NoSuccess       => Left(error(f.msg))
//    }
//  }

//  private def parseConnector(in:String): Either[String,CoreConnector] = {
//    DSL.parseWithError(in) match {
//      case preo.lang.Parser.Success(result,_) =>
//        try {
//          val reduced: Connector = Eval.instantiate(result)
//          Right(Eval.reduce(reduced))
//        } catch {
//          case e: TypeCheckException =>
//            Left(error("Type error: " + e.getMessage))
//          case e: GenerationException =>
//            Left(error("Generation failed: " + e.getMessage))
//        }
//      case preo.lang.Parser.Failure(emsg,_) =>
//        Left(error("Parser failure: " + emsg + " in "+in))
//      case preo.lang.Parser.Error(emsg,_) =>
//        Left(error("Parser error: " + emsg + " in "+in))
//    }
//  }

  private def modelCheck(model: Model,form:String): String = {
    try {
      val id = Thread.currentThread().getId
      storeInFile(model) // create model_id.mcrl2
      minimiseLTS()      // create model_id.lts

      val file = new File(s"/tmp/modal_$id.mu")
      file.setExecutable(true)
      val pw = new PrintWriter(file)
      pw.write(form)
      pw.close()

      val save_output = savepbes()
      if(save_output._1 == 0)
        JsonCreater.create(solvepbes()).toString
      else
        error("Modal Logic failed: " + save_output._2+
           "\n when parsing\n"+form)
    }
    catch {
      case e: java.io.IOException => // by solvepbes/savepbes/storeInFile/generateLTS
        error("IO exception: " + e.getMessage)
    }
  }


  private def view(raw_conn: String, raw_form: String): String = {
    val conn = ParserUtils.parseCoreConnector(raw_conn)
    conn match {
      case Left(s) => s
      case Right(c) =>
        val model = preo.frontend.mcrl2.Model(c)
        storeInFile(model) // save to fie
        callLtsGraph()     // generateLTS
        "ok"
    }
  }

  private def error(e:String) = JsonCreater.createError(e).toString()




}
