package services

import akka.actor._
import preo.backend.Network
import preo.frontend.Eval

import scala.collection.mutable.ListBuffer
import main.scala.reoalloy.{AlloyGenerator, Nodo}


object AlloyActor {
  def props(out:ActorRef) = Props(new AlloyActor(out))
}


class AlloyActor(out: ActorRef) extends Actor{

  private val generator = new AlloyGenerator()
  private val baseModel = prettyPrint(
    """
      |open util/ordering[State] as S
      |
      |sig Node {}
      |
      |abstract sig Connector {
      | conns : set Connector ,
      | ports : set Node
      |}
      |
      |abstract sig Channel extends Connector {
      | e1, e2 : one Node
      |}
      |{
      | ports = e1 + e2
      | conns = none
      |}
      |
      |sig State{ fire : set Node }
      |
      |sig Sync extends Channel {}{
      | all s : State | e1 in s.fire iff e2 in s.fire
      |}
      |
      |pred sync[src : one Node, s : one Sync, sink : one Node]{
      | src = s.e1
      | sink = s.e2
      |}
      |
      |sig Drain extends Channel {}{
      | all s : State | e1 in s.fire iff e2 in s.fire
      |}
      |
      |pred drain[sink1, sink2 : one Node, d : one Drain]{
      | sink1 = d.e1
      | sink2 = d.e2
      |}
      |
      |sig Lossy extends Channel {}{
      | all s : State | e2 in s.fire implies e1 in s.fire
      |}
      |
      |pred lossy[src : one Node, l : one Lossy, sink : one Node]{
      | src = l.e1
      | sink = l.e2
      |}
      |
      |sig Value{}
      |
      |sig Fifo extends Channel { buffer : Value lone -> State }{
      | all s : State - first | let ant = s.prev,  received = e1 in ant.fire, sent = e2 in ant.fire,
      |           emptyBefore = no buffer.ant,  emptyNow = no buffer.s,
      |           fullBefore = some buffer.ant,  fullNow = some buffer.s
      |  {
      |   sent and received  implies fullBefore and fullNow
      |   else{
      |     received implies emptyBefore and fullNow
      |
      |     sent implies fullBefore and emptyNow
      |
      |     not received and not sent implies buffer.ant = buffer.s
      |   }
      | }
      |}
      |
      |pred fifo[src : one Node, f : one Fifo, sink: one Node]{
      | src = f.e1
      | sink = f.e2
      |
      | no f.buffer.first
      |}
      |
      |pred fifofull[src : one Node, f : one Fifo, sink: one Node]{
      | src = f.e1
      | sink = f.e2
      | some  f.buffer.first
      |}
      |
      |sig Merger extends Connector {
      | disj i1, i2, o : one Node
      |}
      |{
      | ports = i1 + i2 + o
      | conns = none
      |}
      |
      |pred merger[inp1, inp2 : one Node, m : one Merger, out : one Node]{
      | inp1 = m.i1
      | inp2 = m.i2
      | out = m.o
      |
      | all s : State | let fire_i1 = inp1 in s.fire, fire_i2 = inp2 in s.fire  {
      |   fire_i1 implies not fire_i2
      |   fire_i2 implies not fire_i1
      |
      |   out in s.fire iff fire_i1 or fire_i2
      | }
      |}
      |
      |sig Replicator extends Connector {
      | disj i, o1, o2 : one Node
      |}
      |{
      | ports = i + o1 + o2
      | conns = none
      |}
      |
      |pred replicator[inp : one Node, r : one Replicator, out1, out2 : one Node]{
      | inp = r.i
      | out1 = r.o1
      | out2 = r.o2
      |
      | all s : State {
      |   inp in s.fire iff out1 in s.fire
      |   inp in s.fire iff out2 in s.fire
      | }
      |}
      |
      |sig VMerger extends Connector {
      | o : one Node,
      | i1, i2 : lone Node
      |}
      |{
      | disj[o, i1, i2]
      | ports = i1 + i2 + o
      | conns = none
      |}
      |
      |pred vmerger[inp1, inp2 : lone Node, m : one VMerger, out : one Node]{
      | inp1 = m.i1
      | inp2 = m.i2
      | out = m.o
      |
      | all s : State | let fire_i1 = m.i1 in s.fire, fire_i2 = m.i2 in s.fire  {
      |   some m.i1 and fire_i1 implies not fire_i2
      |   some m.i2 and fire_i2 implies not fire_i1
      |
      |   some m.i1 + m.i2 implies out in s.fire iff fire_i1 or fire_i2
      | }
      |}
      |
      |sig VDupl extends Connector{
      | i : one Node,
      | o1, o2 : lone Node
      |}{
      | disj[i, o1, o2]
      | ports = i + o1 + o2
      | conns = none
      |}
      |
      |pred vdupl[inp : one Node, r : one VDupl, out1, out2 : lone Node]{
      | inp = r.i
      | out1 = r.o1
      | out2 = r.o2
      |
      | all s : State {
      |   some out1 implies inp in s.fire iff out1 in s.fire
      |   some out2 implies inp in s.fire iff out2 in s.fire
      | }
      |}
      |
      |fact{
      | all c : Connector | c not in c.^conns
      | all s : State - last | s.fire != s.next.fire
      |}
      |
      |
    """.stripMargin)
  
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

      /*print da lista de nodos
      var  s : String = ""

      for (elem <- nodos) {
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

      baseModel + prettyPrint(generator.instanceToString)

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

    prms.foreach( p =>
      {
        val name = p.prim.name
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
