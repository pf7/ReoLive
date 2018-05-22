package services

import akka.actor._
import preo.DSL
import preo.ast.BVal
import preo.common.{GenerationException, TypeCheckException}
import preo.frontend.{Eval, Show, Simplify}

object ReoActor {
  def props(out: ActorRef) = Props(new ReoActor(out))
}

class ReoActor(out: ActorRef) extends Actor {
  def receive = {
    case msg: String =>
      out ! process(msg)
  }

  private def process(msg: String): String = {
    var warnings: List[String] = List()
    var errors: List[String] = List()

    DSL.parseWithError(msg) match {
      case preo.lang.Parser.Success(result,_) =>
        try {
          val typ = DSL.checkVerbose(result)


          val reduc = Eval.instantiate(result)
          val reducType = DSL.typeOf(reduc)
          val coreConnector = Eval.reduce(reduc)

//              // draw connector
//              if(isVisible("Circuit of the instance")) {
//                drawConnector(svg)
//              }
//
//              // draw Automata
//              if (isVisible("Automaton of the instance (under development)")) {
//                //                println("aut pannel is openned")
//                drawAutomata(svgAut)
//              }
//
//              // produce mCRL2
//              if(isVisible("mCRL2 of the instance")) {
//                produceMcrl2()
//              }
        }
        catch {
          // type error
          case e: TypeCheckException =>
            errors ++= List("Type error: " + e.getMessage)
          //            instanceInfo.append("p").text("-")
          case e: GenerationException =>
            warnings ++= List("Generation failed: " + e.getMessage)
          }
      case preo.lang.Parser.Failure(msg,_) =>
        errors ++= List("Parser failure: " + msg)
      //        instanceInfo.append("p").text("-")
      case preo.lang.Parser.Error(msg,_) =>
        errors ++= "Parser error: " + msg)
      //        instanceInfo.append("p").text("-")
    }

  }
}
