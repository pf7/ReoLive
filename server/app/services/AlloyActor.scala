package services

import akka.actor._
import preo.backend.Network
import preo.frontend.Eval

import scala.collection.mutable.ListBuffer
import reoalloy.{AlloyGenerator, Nodo}


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
    val cleanMsg = msg.replace("\\\\", "\\")
      .replace("\\n", "\n")

    try {
      val genConn = preo.DSL.parse(cleanMsg)
      val conn = Eval.reduce(genConn)
      val netw: Network = preo.backend.Network.apply(conn,hideClosed = false)
      val reo = NetworkToLNodo(netw)

      //print da lista de nodos
      /*var  s : String = ""

      for (elem <- reo) {
        s = s + elem.id + "<br>ins "
        for(ins <- elem.entradas){
          s = s + ins + "<br>"
        }
        s = s + "outs "
        for(outs <- elem.saidas){
          s = s + outs + "<br>"
        }

      }*/
      // Create ReoAlloy magic here

      //"Resulting Alloy Program"

      val sol = generator.getAlloy(reo)

      prettyPrint(generator.modelToString)
      
   }
    catch {
      case e: Throwable => "Error: "+e.getMessage
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

            case "mrg" => name = "merger"

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
