package reoalloy

import edu.mit.csail.sdg.alloy4compiler.ast._
import edu.mit.csail.sdg.alloy4compiler.ast.Attr
import edu.mit.csail.sdg.alloy4compiler.ast.Sig._
import edu.mit.csail.sdg.alloy4.{ConstList, Util}

import scala.collection.JavaConverters._
import scala.collection.mutable

class BaseModel {
  /**
    * Node
    */
  private val node = new PrimSig("Node")
  private val node_sig = node.oneOf()
  private val node_set = node.setOf()
  private val node_lone = node.loneOf()

  /**
    * Connector
    */
  private val connector = new PrimSig("Connector", Attr.ABSTRACT)
  private val conns = connector.addField("conns", connector.setOf())
  private val ports = connector.addField("ports", node_set)
  private val c = connector.oneOf("c")

  /**
    * State
    */
  private val state = new PrimSig("State")
  private val fire = state.addField("fire", node_set)
  private val first = new PrimSig("First", state, Attr.ONE)
  private val next = state.addField("next", state.loneOf())
  private val order : List[_ <: Expr] = List(state, first, next)
  state.addFact(ExprList.makeTOTALORDER(null, null, order.asJava))

  /**
    * Channel
    */
  private val channel = new PrimSig("Channel", connector, Attr.ABSTRACT)
  private val e1 = channel.addField("e1", node_sig)
  private val e2 = channel.addField("e2", node_sig)
  channel.addFact(channel.join(e1).plus(channel.join(e2)).equal(channel.join(ports)))
  channel.addFact(channel.join(conns).no())
  disj(channel, List[Expr](e1, e2))

  /**
    * Circuit
    */
  //private val circuit = new PrimSig("Circuit", Attr.ONE)
  //private val root = circuit.addField("root", connector.oneOf())

  /**
    * Value
    */
  private val value = new PrimSig("Value")

  //Declarações auxiliares para definição dos conetores
  private val s = state.oneOf("s")
  private val src = node.oneOf("src")
  private val sink = node.oneOf("sink")

  //Associa o id do conetor de base ao respetivo predicado
  private val baseConnPred = mutable.Map[String, Func]()
  //Sigs dos conetores
  private val connectors = buildConn

  /**
    * Factos globais
    */
  UNIV.addFact(c.get().in(c.get().join(conns.closure())).not().forAll(c))
  //UNIV.addFact(c.get().equal(circuit.join(root)).not().implies(c.get().in(circuit.join(root).join(conns.closure()))).forAll(c))
  //UNIV.addFact(circuit.join(root).in(c.get().join(conns)).not().forAll(c))

  //UNIV.addFact(s.get().join(next).some().implies(s.get().join(fire).equal(s.get().join(next).join(fire)).not()).forAll(s))
  //Predicado cantFire: true se para o estado e nodo especificados, o nodo não está em condições de disparar nesse estado.
  private val n_node = node.oneOf("n")
  private val f_fifo = connectors("fifo").oneOf("f")
  private val f_e1 = f_fifo.get().join(e1)
  private val f_e2 = f_fifo.get().join(e2)
  private val f_buffer = f_fifo.get().join(connectors("fifo").getFields.get(0))
  private val m_merger = connectors("merger").oneOf("m")
  private val m_i1 = m_merger.get().join(connectors("merger").getFields.get(0))
  private val m_i2 = m_merger.get().join(connectors("merger").getFields.get(1))
  private val vm_vmerger = connectors("vmerger").oneOf("vm")
  private val vm_i1 = vm_vmerger.get().join(connectors("vmerger").getFields.get(0))
  private val vm_i2 = vm_vmerger.get().join(connectors("vmerger").getFields.get(1))
  private val cantFire = new Func(null, "cantFire", Util.asList(s, n_node), null,
   // n_node.get().equal(f_e1).and(f_buffer.join(s.get()).some()).and(f_e2.in(s.get().join(fire)).not()).or(
    n_node.get().equal(f_e1).and(f_buffer.join(s.get()).some()).or(
      n_node.get().equal(f_e2).and(f_buffer.join(s.get()).no())
      ).`forSome`(f_fifo)
    .or(
      n_node.get().equal(m_i1).and(m_i2.in(s.get().join(fire))).or(
        n_node.get().equal(m_i2).and(m_i1.in(s.get().join(fire)))
      ).`forSome`(m_merger)
    ).or(
      n_node.get().equal(vm_i1).and(vm_i2.some()).and(vm_i2.in(s.get().join(fire))).or(
        n_node.get().equal(vm_i2).and(vm_i1.some()).and(vm_i1.in(s.get().join(fire)))
      ).`forSome`(vm_vmerger)
    )
  )
  UNIV.addFact(
    s.get().join(next).some().implies(
      s.get().join(fire).equal(s.get().join(next).join(fire)).and(s.get().join(fire).no()).implies(
        cantFire.call(s.get(), n_node.get()).forAll(n_node)
      )
    ).forAll(s)
  )

  //Função nodes: Determina todos os nodos abrangidos pelo connector especificado
  /*private val nodes = new Func(null, "nodes", Util.asList(c), node_set, c.get().join(ports).plus(c.get().join(conns.closure()).join(ports)))
  private val n = node.oneOf("n")
  UNIV.addFact(n.get().in(nodes.call(circuit.join(root))).forAll(n))*/

  /**
    * Theme Functions
    */
  private val fireNodes = new Func(null, "FireNodes", null, state.product(node), n_node.get().in(s.get().join(fire)).comprehensionOver(s, n_node))
  private val emptyFifo = new Func(null, "EmptyFifo", null, state.product(connectors("fifo")), f_buffer.join(s.get()).no().comprehensionOver(s, f_fifo))
  private val fullFifo = new Func(null, "FullFifo", null, state.product(connectors("fifo")), f_buffer.join(s.get()).some().comprehensionOver(s, f_fifo))

  /**
    * sig @sig ...{
    *   args
    *   ...
    * }{
    *   disj[args]
    *   ...
    * }
    * @param sig
    * @param args
    */
  private def disj(sig : Sig, args : List[_ <: Expr]): Unit = {
    sig.addFact(ExprList.makeDISJOINT(null, null, args.asJava))
  }

  /**
    * Sync
    * @return
    */
  private def getSync: Sig ={
    val sync = new PrimSig("Sync", channel)
    sync.addFact(sync.join(e1).in(s.get().join(fire)).iff(
      sync.join(e2).in(s.get().join(fire))).forAll(s))

    /* Predicado */
    val s_sync = sync.oneOf("s")
    val pSyncBody = src.get().equal(s_sync.get().join(e1)).and(
      sink.get().equal(s_sync.get().join(e2))
    )
    val pSync = new Func(null, "sync", Util.asList(src, s_sync, sink), null, pSyncBody)
    baseConnPred("sync") = pSync

    sync
  }

  /**
    * Drain
    * @return
    */
  private def getDrain : Sig ={
    val drain = new PrimSig("Drain", channel)
    drain.addFact(drain.join(e1).in(s.get().join(fire)).iff(
      drain.join(e2).in(s.get().join(fire))).forAll(s))

    /* Predicado */
    val sink1 = node.oneOf("sink1")
    val sink2 = node.oneOf("sink2")

    val d_drain = drain.oneOf("d")
    val pDrainBody = sink1.get().equal(d_drain.get().join(e1)).and(
      sink2.get().equal(d_drain.get().join(e2))
    )

    val pDrain = new Func(null, "drain", Util.asList(sink1, sink2, d_drain), null, pDrainBody)
    baseConnPred("drain") = pDrain

    drain
  }

  /**
    * Lossy
    * @return
    */
  private def getLossy : Sig ={
    val lossy = new PrimSig("Lossy", channel)
    lossy.addFact(lossy.join(e2).in(s.get().join(fire))
      .implies(lossy.join(e1).in(s.get().join(fire)))
      .forAll(s))

    /* Predicado */
    val l_lossy = lossy.oneOf("l")
    val pLossyBody = src.get().equal(l_lossy.get().join(e1)).and(
      sink.get().equal(l_lossy.get().join(e2))
    )
    val pLossy = new Func(null, "lossy", Util.asList(src, l_lossy, sink), null, pLossyBody)
    baseConnPred("lossy") = pLossy

    lossy
  }

  /**
    * FIFO
    * @return
    */
  private def getFifo : Sig ={
    val fifo = new PrimSig("Fifo", channel)
    val buffer = fifo.addField("buffer", value.lone_arrow_any(state))

    /*val ant = ExprVar.make(null, "ant")
    val received = ExprVar.make(null, "received")
    val sent = ExprVar.make(null, "sent")
    val emptyBefore = ExprVar.make(null, "emptyBefore")
    val emptyNow = ExprVar.make(null, "emptyNow")
    val fullBefore = ExprVar.make(null, "fullBefore")
    val fullNow = ExprVar.make(null, "fullNow")*/

    /*fifo.addFact(
      ExprLet.make(null, ant, s.get().join(next.transpose()),
      ExprLet.make(null, received, fifo.join(e1).in(ant.join(fire)),
      ExprLet.make(null, sent, fifo.join(e2).in(ant.join(fire)),
      ExprLet.make(null, emptyBefore, fifo.join(buffer).join(ant).no(),
      ExprLet.make(null, emptyNow, fifo.join(buffer).join(s.get()).no(),
      ExprLet.make(null, fullBefore, fifo.join(buffer).join(ant).some(),
      ExprLet.make(null, fullNow, fifo.join(buffer).join(s.get()).some(),
        s.get().equal(first).not().implies(
            sent.and(received).ite(fullBefore.and(fullNow),
                 received.implies(emptyBefore.and(fullNow)).and(
                 sent.implies(fullBefore.and(emptyNow))).and(
                 received.not().and(sent.not()).implies(fifo.join(buffer).join(ant).equal(fifo.join(buffer).join(s.get())))
             ))).forAll(s)))))))))

    fifo.addFact(
      s.get().equal(first).not().implies(
        fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).and(fifo.join(e1).in(s.get().join(next.transpose()).join(fire))).ite(fifo.join(buffer).join(s.get().join(next.transpose())).some().and(fifo.join(buffer).join(s.get()).some()),
          fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).implies(fifo.join(buffer).join(s.get().join(next.transpose())).no().and(fifo.join(buffer).join(s.get()).some())).and(
            fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).implies(fifo.join(buffer).join(s.get().join(next.transpose())).some().and(fifo.join(buffer).join(s.get()).no()))).and(
            fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).not().and(fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).not()).implies(fifo.join(buffer).join(s.get().join(next.transpose())).equal(fifo.join(buffer).join(s.get()))
            )))).forAll(s))*/


    /*fifo.addFact(
      ExprLet.make(null, ant, s.get().join(next.transpose()),
      ExprLet.make(null, received, fifo.join(e1).in(ant.join(fire)),
      ExprLet.make(null, sent, fifo.join(e2).in(ant.join(fire)),
      ExprLet.make(null, emptyBefore, fifo.join(buffer).join(ant).no(),
      ExprLet.make(null, emptyNow, fifo.join(buffer).join(s.get()).no(),
      ExprLet.make(null, fullBefore, fifo.join(buffer).join(ant).some(),
      ExprLet.make(null, fullNow, fifo.join(buffer).join(s.get()).some(),
        s.get().equal(first).not().implies(
            received.implies(emptyBefore.and(fullNow).and(sent.not()).and(
            sent.implies(fullBefore.and(emptyNow).and(received.not()))).and(
            received.not().and(sent.not()).implies(fifo.join(buffer).join(ant).equal(fifo.join(buffer).join(s.get())))
          ))).forAll(s)))))))))*/

    /* ???
      fifo.addFact(
      s.get().equal(first).not().implies(
        fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).implies(fifo.join(buffer).join(s.get().join(next.transpose())).no().and(fifo.join(buffer).join(s.get()).some()).and(fifo.join(e2).in(s.get().join(next.transpose()).join(fire).not()).and(
          fifo.join(e2).in(s.get().join(next.transpose()).join(fire).implies(fifo.join(buffer).join(s.get().join(next.transpose())).some().and(fifo.join(buffer).join(s.get()).no()).and(fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).not()))).and(
            fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).not().and(fifo.join(e2).in(s.get().join(next.transpose()).join(fire).not()).implies(fifo.join(buffer).join(s.get().join(next.transpose())).equal(fifo.join(buffer).join(s.get())))
            )))))).forAll(s))*/

    fifo.addFact(
      s.get().equal(first).not().implies(
        fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).implies(fifo.join(buffer).join(s.get().join(next.transpose())).no().and(fifo.join(buffer).join(s.get().join(next.transpose())).some()).and(fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).not()).and(
        fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).implies(fifo.join(buffer).join(s.get().join(next.transpose())).some().and(fifo.join(buffer).join(s.get()).no()).and(fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).not()))).and(
        fifo.join(e1).in(s.get().join(next.transpose()).join(fire)).not().and(fifo.join(e2).in(s.get().join(next.transpose()).join(fire)).not()).implies(fifo.join(buffer).join(s.get().join(next.transpose())).equal(fifo.join(buffer).join(s.get())))
      ))).forAll(s))


    /* Predicado p/ FIFO */
    val f_fifo = fifo.oneOf("f")
    val pFifoBody = src.get().equal(f_fifo.get().join(e1)).and(
      sink.get().equal(f_fifo.get().join(e2))).and(
      f_fifo.get().join(buffer.join(first)).no())

    val pFifo = new Func(null, "fifo", Util.asList(src, f_fifo, sink), null, pFifoBody)
    baseConnPred("fifo") = pFifo

    /* Predicado p/ FIFOFULL */
    val pFifoFullBody = src.get().equal(f_fifo.get().join(e1)).and(
      sink.get().equal(f_fifo.get().join(e2))).and(
      f_fifo.get().join(buffer.join(first)).some())

    val pFifoFull = new Func(null, "fifofull", Util.asList(src, f_fifo, sink), null, pFifoFullBody)
    baseConnPred("fifofull") = pFifoFull

    fifo
  }

  /**
    * Merger
    * @return
    */
  private def getMerger : Sig ={
    val merger = new PrimSig("Merger", connector)

    val i1 = merger.addField("i1", node_sig)
    val i2 = merger.addField("i2", node_sig)
    val o = merger.addField("o", node_sig)

    merger.addFact(merger.join(i1).plus(merger.join(i2)).plus(merger.join(o)).equal(merger.join(ports)))
    merger.addFact(merger.join(conns).no())
    disj(merger, List[Expr](i1, i2, o))

    /* Predicado */
    val m_merger = merger.oneOf("m")
    val inp1 = node.oneOf("inp1")
    val inp2 = node.oneOf("inp2")
    val out = node.oneOf("out")

    val fire_i1 = ExprVar.make(null, "fire_i1", Type.FORMULA)
    val fire_i2 = ExprVar.make(null, "fire_i2", Type.FORMULA)

    val m_comp = ExprLet.make(null, fire_i1, inp1.get().in(s.get().join(fire)),
      ExprLet.make(null, fire_i2, inp2.get().in(s.get().join(fire)),
        fire_i1.implies(fire_i2.not()).and(
          fire_i2.implies(fire_i1.not())).and(
          out.get().in(s.get().join(fire)).iff(fire_i1.or(fire_i2)))))
      .forAll(s)

    val pMergerBody = inp1.get().equal(m_merger.get().join(i1)).and(
      inp2.get().equal(m_merger.get().join(i2))).and(
      out.get().equal(m_merger.get().join(o))).and(
      m_comp
    )

    val pMerger = new Func(null, "merger", Util.asList(inp1, inp2, m_merger, out), null, pMergerBody)
    baseConnPred("merger") = pMerger

    merger
  }

  /**
    * Dupl
    * @return
    */
  private def getReplicator : Sig ={
    val dupl = new PrimSig("Replicator", connector)

    val i = dupl.addField("i", node_sig)
    val o1 = dupl.addField("o1", node_sig)
    val o2 = dupl.addField("o2", node_sig)

    dupl.addFact(dupl.join(i).plus(dupl.join(o1)).plus(dupl.join(o2)).equal(dupl.join(ports)))
    dupl.addFact(dupl.join(conns).no())
    disj(dupl, List[Expr](i, o1, o2))

    /* Predicado */
    val r_dupl = dupl.oneOf("r")
    val inp = node.oneOf("inp")
    val out1 = node.oneOf("out1")
    val out2 = node.oneOf("out2")

    /*val s_fire = ExprVar.make(null, "s_fire", Type.FORMULA)

    val pDuplBody = ExprLet.make(null, s_fire, s.get().join(fire),
                    inp.get().in(s_fire).iff(out1.get().in(s_fire)).and(
                    inp.get().in(s_fire).iff(out2.get().in(s_fire))
    )).forAll(s)*/

    val pDuplBody =  inp.get().in(s.get().join(fire)).iff(out1.get().in(s.get().join(fire))).and(
      inp.get().in(s.get().join(fire)).iff(out2.get().in(s.get().join(fire)))
    ).forAll(s)

    val pDupl = new Func(null, "replicator", Util.asList(inp, r_dupl, out1, out2), null, pDuplBody)
    baseConnPred("dupl") = pDupl

    dupl
  }

  /**
    * VMerger
    * @return
    */
  private def getVMerger : Sig ={
    val vmerger = new PrimSig("VMerger", connector)

    val i1 = vmerger.addField("i1", node_lone)
    val i2 = vmerger.addField("i2", node_lone)
    val o = vmerger.addField("o", node_sig)

    vmerger.addFact(vmerger.join(i1).plus(vmerger.join(i2)).plus(vmerger.join(o)).equal(vmerger.join(ports)))
    vmerger.addFact(vmerger.join(conns).no())
    disj(vmerger, List[Expr](i1, i2, o))

    /* Predicado */
    val m_vmerger = vmerger.oneOf("m")
    val inp1 = node.loneOf("inp1")
    val inp2 = node.loneOf("inp2")
    val out = node.oneOf("out")

    val fire_i1 = ExprVar.make(null, "fire_i1", Type.FORMULA)
    val fire_i2 = ExprVar.make(null, "fire_i2", Type.FORMULA)

    val vm_comp = ExprLet.make(null, fire_i1, inp1.get().in(s.get().join(fire)),
      ExprLet.make(null, fire_i2, inp2.get().in(s.get().join(fire)),
        inp1.get().some().and(fire_i1).implies(fire_i2.not()).and(
          inp2.get().some().and(fire_i2).implies(fire_i1.not())).and(
          inp1.get().plus(inp2.get()).some().implies(out.get().in(s.get().join(fire)).iff(fire_i1.or(fire_i2)))
        ))).forAll(s)

    val pVMergerBody = inp1.get().equal(m_vmerger.get().join(i1)).and(
      inp2.get().equal(m_vmerger.get().join(i2))).and(
      out.get().equal(m_vmerger.get().join(o))).and(
      vm_comp
    )

    val pVMerger = new Func(null, "vmerger", Util.asList(inp1, inp2, m_vmerger, out), null, pVMergerBody)
    baseConnPred("vmerger") = pVMerger

    vmerger
  }

  /**
    * VDupl
    * @return
    */
  private def getVDupl : Sig ={
    val vdupl = new PrimSig("VDupl", connector)

    val i = vdupl.addField("i", node_sig)
    val o1 = vdupl.addField("o1", node_lone)
    val o2 = vdupl.addField("o2", node_lone)

    vdupl.addFact(vdupl.join(i).plus(vdupl.join(o1)).plus(vdupl.join(o2)).equal(vdupl.join(ports)))
    vdupl.addFact(vdupl.join(conns).no())
    disj(vdupl, List[Expr](i, o1, o2))

    /* Predicado */
    val r_vdupl = vdupl.oneOf("r")
    val inp = node.oneOf("inp")
    val out1 = node.loneOf("out1")
    val out2 = node.loneOf("out2")

    /*val s_fire = ExprVar.make(null, "s_fire", Type.FORMULA)

    val pVDuplBody = ExprLet.make(null, s_fire, s.get().join(fire),
                      out1.get().some().implies(inp.get().in(s_fire).iff(out1.get().in(s_fire))).and(
                      out2.get().some().implies(inp.get().in(s_fire).iff(out2.get().in(s_fire)))
                    )).forAll(s)*/

    val pVDuplBody = out1.get().some().implies(inp.get().in(s.get().join(fire)).iff(out1.get().in(s.get().join(fire)))).and(
      out2.get().some().implies(inp.get().in(s.get().join(fire)).iff(out2.get().in(s.get().join(fire))))
    ).forAll(s)

    val pVDupl = new Func(null, "vdupl", Util.asList(inp, r_vdupl, out1, out2), null, pVDuplBody)
    baseConnPred("vdupl") = pVDupl

    vdupl
  }

  /**
    * Constrói o Map com as sigs de base (que não representam os conetores básicos)
    * @return
    */
  def getBaseSigs : mutable.Map[String, Sig] ={
    val baseSigs = mutable.Map[String, Sig]()

    baseSigs("Node") = node
    baseSigs("Connector") = connector
    baseSigs("State") = state
    baseSigs("First") = first
    baseSigs("Channel") = channel
    //baseSigs("Circuit") = circuit
    baseSigs("Value") = value

    baseSigs
  }

  /**
    * Definição dos conetores de base
    * @return
    */
  private def buildConn : mutable.Map[String, Sig] = {
    val connSigs = mutable.Map[String, Sig]()

    connSigs("sync") = getSync
    connSigs("drain") = getDrain
    connSigs("lossy") = getLossy
    connSigs("fifo") = getFifo
    connSigs("fifofull") = connSigs("fifo")
    connSigs("merger") = getMerger
    connSigs("dupl") = getReplicator
    connSigs("vmerger") = getVMerger
    connSigs("vdupl") = getVDupl

    connSigs
  }

  def getConnSigs : mutable.Map[String, Sig] = connectors

  def getConnPred : mutable.Map[String, Func] = baseConnPred

  /**
    * @return Sigs do modelo de base
    */
  def getModel : ConstList[Sig] = ConstList.make((connectors.values.toList.distinct ++ getBaseSigs.values.toList ++ List(NONE, STRING, UNIV, SEQIDX, SIGINT)).asJava)

  /**
    * Devolve a raíz do circuito
    * @return
    */
  //def getRoot : Expr = circuit.join(root)

  /**
    * Retorna as funções auxiliares para o tema
    * @return
    */
  def getThemeFunc : Iterable[Func] = List(fireNodes, emptyFifo, fullFifo)
}
