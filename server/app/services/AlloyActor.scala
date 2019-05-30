package services

import akka.actor._
import preo.backend.Network
import preo.frontend.Eval

import scala.collection.mutable.ListBuffer
import reoalloy.{AlloyGenerator, Nodo}


import play.api.libs.json._

object AlloyActor {
  def props(out:ActorRef) = Props(new AlloyActor(out))
}


class AlloyActor(out: ActorRef) extends Actor{

  private val generator = new AlloyGenerator()

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
    * Get a request to produce a graphic of a hprog.
    * @param msg
    * @return
    */
  private def process(msg: String): String = {

    //var msgContent : Array[String] = Array(msg)

    //Se a mensagem começa por um número N
    //N representa o #instância que queremos mostrar
    /*if(msg.matches("""^\d+.*""")){
      //0 -> Representa o Circuito
      //1 -> N
      msgContent = msg.split("""(?<=\d+)""", 2).reverse
    }
    else if(msg.matches("""^CHECK__.+?__\d+__.*""")){
      //Verificação de Propriedades
      //0 -> Circuito
      //1 -> N
      //2 -> Propriedade
      msgContent = msg.split("__", 4).reverse
    }*/

    //val cleanMsg = msgContent(0).as[String].replace("\\\\", "\\")
    //        .replace("\\n", "\n")

    val cleanMsg = msg.replace("\\\\", "\\")
        .replace("\\n", "\n")

    val msgContent : JsValue = Json.parse(cleanMsg)


    try {
      //val genConn = preo.DSL.parse(cleanMsg)
      val genConn = preo.DSL.parse(msgContent("reo").as[String])
      val conn = Eval.reduce(genConn)
      val netw: Network = preo.backend.Network.apply(conn, hideClosed = false)
      val reo = NetworkToLNodo(netw)

      var sol = generator.getSolutions(reo)

      //if(msgContent.length > 1){
      if(msgContent("solve").as[Boolean] || msgContent("check").as[Boolean]){
        var unsat = "UNSAT"

        //CHECK
        //if(msgContent.length == 4) {
        if(msgContent("check").as[Boolean]){
          //sol = generator.checkProperty(reo, msgContent(2))
          sol = generator.checkProperty(reo, msgContent("prop").as[String])
          unsat = "CHECK_UNSAT"
        }

        //val n_sol = msgContent(1).toInt
        val n_sol = msgContent("num").as[Int]

        for(i <- 0 to n_sol){
          sol = sol.next
        }

        if(sol.satisfiable()){
          reoalloy.Main.showViz(sol)
          "SAT"
        }
        else unsat

      }
      else prettyPrint(generator.modelToString)

      }
      catch {
        case e: Throwable => "Error: " + e.getMessage
      }

  }

  /**
    * Transforma uma Network -> Lista de Nodos para input do AlloyGenerator
    *
    * @param netw
    * @return
    */
  def NetworkToLNodo(netw:Network): List[Nodo] ={
    val prms = netw.prims
    var lnodo : ListBuffer[Nodo] = new ListBuffer[Nodo]()
    val validBaseConn = Array("sync", "lossy", "drain", "fifo", "fifofull", "node", "dupl", "vdupl", "merger", "vmerger")

    prms.foreach( p =>
      {
        var name = ""
        if(p.prim.extra.isEmpty) {

          if(validBaseConn contains p.prim.name)
            name = p.prim.name
          else
            //Se o id não identificar um conetor de base, assumimos que corresponde a um sync.
            name = "sync"

        }
        else{
          name = p.prim.extra.head.toString

          name match {
            case "vmrg" => name = "vmerger"

            case "mrg" => name = "node"

            case "dupl" => name = "node"

            case _ => ;
          }

        }

        val ins = p.ins
        val outs = p.outs


        val n = new Nodo(name,ins,outs)
        lnodo += n
      }
    )
    lnodo.toList
  }

  /**
    * PrettyPrint de código Alloy.
    * @param alloy
    */
  def prettyPrint(alloy : String): String = {
      alloy.replaceAll("\n", "<br>")
           .replaceAll(" ", "&nbsp;")
           .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
           .replaceAll("(sig|disj|open|lone|none|one|&nbsp;as&nbsp;|" +
            "set|pred|abstract|all|some|extends|iff|&nbsp;in&nbsp;|" +
            "and|&nbsp;or&nbsp;|implies|let|not|&nbsp;no&nbsp;|else|fact|fun)",
            "<b><font color=\"#1E1EA8\">$1</font></b>")
  }

//  private def callSage(prog: String, sagePath:String): String = try {
//    val syntax = hprog.DSL.parse(prog)
//    val eqs = hprog.frontend.Utils.getDiffEqs(syntax)
//    val replySage = hprog.frontend.SageSolver.callSageSolver(eqs,sagePath,timeout=10)
//    replySage.mkString("§")
//  }
//  catch {
//    case p:ParserException =>
//      //println("failed parsing: "+p.toString)
//      s"Error: When parsing ${prog} - ${p.toString}"
//    case e:Throwable => "Error "+e.toString
//  }



}
