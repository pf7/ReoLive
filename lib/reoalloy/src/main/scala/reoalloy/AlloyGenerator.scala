package reoalloy

import edu.mit.csail.sdg.alloy4compiler.ast._
import edu.mit.csail.sdg.alloy4compiler.ast.Sig._
import edu.mit.csail.sdg.alloy4.{A4Reporter, ConstList, Util}
import edu.mit.csail.sdg.alloy4.A4Reporter.NOP
import edu.mit.csail.sdg.alloy4compiler.parser.{CompModule, CompUtil}
import edu.mit.csail.sdg.alloy4compiler.translator.{A4Options, A4Solution, TranslateAlloyToKodkod}

import scala.collection.mutable
import scala.collection.JavaConverters._

class AlloyGenerator() {

  //private val baseModel = new BaseModel()

  //Sigs que compõem o modelo (exceto a dos conetores)
  private val baseSigs = mutable.Map[String, Sig]()
  //private val baseSigs = baseModel.getBaseSigs
  //Sigs dos conetores de base
  private val connSigs = mutable.Map[String, Sig]()
  //private val connSigs = baseModel.getConnSigs
  //Predicados para especificar cada conetor de base
  private val connPred = mutable.Map[String, Func]()
  //private val connPred = baseModel.getConnPred


  //Alloy World
  private var world : Module = null
  private def loadModel(): List[Sig] = {
    world = CompUtil.parseEverything_fromFile(new A4Reporter(), null, "./lib/reoalloy/model/Reo.als")

    world.getAllSigs.forEach(s =>
      s.label match {
        case "this/Fifo" =>
          connSigs("fifo") = s
          connSigs("fifofull") = s

        case "this/Replicator" =>
          connSigs("dupl") = s

        case "this/Node" | "this/Connector" | "this/Channel" | "this/State" | "this/Value" => //| "this/Circuit" =>
          baseSigs(s.label.substring(5)) = s

        case _ => connSigs(s.label.substring(5).toLowerCase) = s

      }
    )

    world.getAllFunc.forEach(f =>
      f.label match {
        case "this/replicator" =>
          connPred("dupl") = f

        case _ =>
          connPred(f.label.substring(5)) = f
      }
    )

    var model : List[Sig] = List()
    world.getAllReachableSigs.forEach(s => model = s :: model)

    model
  }

  //Modelo de Base
  private val baseModel : List[Sig] = loadModel()

  /**
    * Identificar a correspondência de 'node'
    * node [a, b] [c] -> Merger
    * node [a] [b, c] -> Replicator
    * node [a, b] [c, d] -> Merger ; Replicator
    * -> merger [a, b] [ab_cd] ; replicator [ab_cd] [c, d]
    * @param reo Circuito
    * @return
    */
  private def unfoldNode(reo: List[Nodo]) : List[Nodo] = {
    var newReo = reo
    var maxId = 0

    //Reset ao conjunto
    connInCircuit = Set()

    //TO DO: melhor forma de encontrar ID para nodo intermédio
    //evitando percorrer a estrutura mais que uma vez
    for(n <- newReo) {
      var inM = -1
      if(n.entradas.nonEmpty)
        inM = n.entradas.max

      var outM = -1
      if(n.saidas.nonEmpty)
        outM = n.saidas.max

      if(maxId < inM)
        maxId = inM

      if(maxId < outM)
        maxId = outM
    }

    maxId += 1

    for(n <- newReo) {

      if(n.id == "node"){
        val nOut = n.saidas.size
        val nIn = n.entradas.size

        nOut + nIn match {
          case 4 =>
            //Dividimos o nodo em dois
            val n2 = new Nodo("dupl", List(maxId), n.saidas)
            newReo = n2 :: newReo
            //Atualizamos informação do nodo ja existente
            n.id = "merger"
            n.saidas = List(maxId)

            //Atualizar maxId
            maxId += 1

            //Especificar novo nodo no conjunto
            connInCircuit += "dupl"

          case _ =>

            if(nOut == 2){
              //Estamos perante um Replicator
              n.id = "dupl"
            }
            else{
              //Trata-se de um Merger
              n.id = "merger"
            }

        }
      }

      //Atualizamos o conjunto
      if(n.id == "fifofull"){
        connInCircuit += "fifo"
      }
      else connInCircuit += n.id

    }

    newReo
  }

  /**
    * Propriedades de Instance
    */
  private var inst_expr : Expr = NONE
  /**
    * Representa a sig Instance produzida para o último circuito processado.
    */
  private var currentInst : Sig = NONE
  /**
    * Auxiliares para representação do modelo como string
    */
  private var currentOneNode : Set[String] = Set()
  private var currentLoneNode : Set[String] = Set()
  private var currentConn : mutable.Map[String, Set[String]] = mutable.Map[String, Set[String]]()
  //Declarações dos conetores básicos
  private val connDecl: mutable.Map[String,String] = mutable.Map[String, String]()
  initConnDecl()
  //Contém os identificadores dos conetores que compõem o circuito a ser processado num dado momento
  private var connInCircuit : Set[String] = Set()


  /**
    * Definir a instância associada ao Reo
    * @param reo Circuito Reo
    * @return
    */
  def getInstance(reo : List[Nodo]) : Sig = {
    //val inst = new PrimSig("Instance", baseSigs("Connector").asInstanceOf[PrimSig], Attr.ONE)
    val inst = new PrimSig("Instance", Attr.ONE)
    val node_sig =  baseSigs("Node").oneOf()
    val node_lone = baseSigs("Node").loneOf()
    val node_set = baseSigs("Node").setOf()

    //?
    val conns = inst.addField("conns", baseSigs("Connector").setOf())
    val ports = inst.addField("ports", node_set)

    //Fields
    val fields = mutable.Map[String, Sig.Field]()
    //Source nodes
    var in = Set[String]()
    //Sink nodes
    var out = Set[String]()

    //K = id do conetor de base ; V = #de ocorrências na configuração especificada
    val baseConnNr = mutable.Map[String, Int]().withDefaultValue(0)

    //Representa c1 + c2 + .. + c3, para especificar conns = c_sum
    var c_sum : Expr = NONE

    //Nodos do circuito
    var disj : List[_ <: Expr] = List()

    //Auxiliares para instanceToString
    currentOneNode = Set()
    currentLoneNode  = Set()
    currentConn  = mutable.Map[String, Set[String]]()

    //val reo = unfoldNode(n_reo)

    for(nodo <- reo){
      //Identifica os argumentos do predicado que define o conetor 'nodo'
      //Ex: sync[src, s, sink] => args = [src, s, sink]
      var args : Array[Expr] = Array()

      /*============================================ */
      //Nodos de entrada
      /*============================================ */
      nodo.entradas.foreach(e => {
        val label = "x" + e
        //Verificar se o nodo já é um field da sig
        if(!fields.contains(label)){
          //Se não for, adicionamos e armazenamos em fields
          if(nodo.id == "vmerger") {
            fields(label) = inst.addField(label, node_lone)
            currentLoneNode += label
          }
          else {
            fields(label) = inst.addField(label, node_sig)
            currentOneNode += label
          }

          disj = fields(label) :: disj
        }

        //Adicionamos aos parametros
        args = args :+ inst.join(fields(label))

        //Adicionamos ao conjunto dos nodos de entrada
        in += label
      })

      /*============================================ */
      //Connector
      /*============================================ */
      //Atualizar o número de ocorrências
      baseConnNr(nodo.id) += 1

      //Adicionar aos fields de Instance
      val conLabel = nodo.id + baseConnNr(nodo.id)
      fields(conLabel) = inst.addField(conLabel, connSigs(nodo.id))

      if(currentConn.exists(_._1 == nodo.id)){
        currentConn(nodo.id) += conLabel
      } else{
        currentConn(nodo.id) = Set(conLabel)
      }


      //Adicionar aos argumentos
      args = args :+ inst.join(fields(conLabel))

      //Adicionar a lista aos conns
      c_sum = c_sum.plus(inst.join(fields(conLabel)))

      /*============================================ */
      //Nodos de saída
      /*============================================ */
      nodo.saidas.foreach(e => {
        val label = "x" + e
        //Verificar se o nodo já é um field da sig
        if(!fields.contains(label)){
          //Se não for, adicionamos e armazenamos em fields
          if(nodo.id == "vdupl") {
            fields(label) = inst.addField(label, node_lone)
            currentLoneNode += label
          }
          else {
            fields(label) = inst.addField(label, node_sig)
            currentOneNode += label
          }

          disj = fields(label) :: disj
        }

        //Adicionamos aos argumentos
        args = args :+ inst.join(fields(label))

        //Adicionamos ao conjunto dos nodos de saída
        out += label
      })

      //Estamos em condições de especificar este conetor do circuito
      inst.addFact(connPred(nodo.id).call(args:_*))
    }

    /*============================================ */
    //Definir conns
    /*============================================ */
    /*val c_f = baseSigs("Connector").getFields
    val conns = c_f.get(0)
    val ports = c_f.get(1)*/

    inst.addFact(inst.join(conns).equal(c_sum))

    /*============================================ */
    //Declarar portas
    /*============================================ */
    //Identificamos os boundary nodes
    val hidden = in.intersect(out)
    //H = I & O
    //B = (I - H) U (O - H)
    val boundary = in.diff(hidden).union(out.diff(hidden))

    var p_sum : Expr = NONE
    boundary.foreach(p => p_sum = p_sum.plus(inst.join(fields(p))))

    inst.addFact(inst.join(ports).equal(p_sum))

    //Garantir que os nodos são disjuntos
    if(disj.size > 1)
      inst.addFact(ExprList.makeDISJOINT(null, null, disj.asJava))

    /*============================================ */
    //Propriedades de Instance
    /*============================================ */
    /*val circuit = baseSigs("Circuit")
    //Circuit.root in Instance
    inst_expr = circuit.join(circuit.getFields.get(0)).in(inst)*/

    val c = baseSigs("Connector").oneOf("c")
    val conns_c = baseSigs("Connector").getFields.get(0)
    val ports_c = baseSigs("Connector").getFields.get(1)

    //all c : Connector | c in Instance.conns + Instance.conns.^conns
    inst_expr = c.get().in(inst.join(conns).plus(inst.join(conns).join(conns_c.closure()))).forAll(c)

    //Função nodes: Determina todos os nodos abrangidos por Instance
    val nodes = new Func(null, "nodes", null, node_set,
      inst.join(ports).plus(inst.join(conns).join(ports_c)).plus(inst.join(conns).join(conns_c.closure()).join(ports_c)))

    //all n : Node | n in nodes
    val n = baseSigs("Node").oneOf("n")
    inst_expr = inst_expr.and(
      n.get().in(nodes.call()).forAll(n)
    )

    inst
  }


  /**
    * Determina o modelo Alloy associado ao Reo.
    * @param reo Reo
    * @return
    */
  def getAlloy(reo : List[Nodo]) : (ConstList[Sig], Command) = {
    val expl_reo = unfoldNode(reo)

    /* #Mínimo para o Scope (c/ min default = 10)*/
    var N = expl_reo.flatMap(n => n.entradas ++ n.saidas).distinct.size
    if(N < 10) N = 10

    val i = getInstance(expl_reo)
    currentInst = i

    val cmd = new Command(false, N, -1, -1, inst_expr).change(baseSigs("State"), true, N)

    //BaseModel
    //(ConstList.make((i :: baseSigs.values.toList ++ connSigs.values.toList).distinct.asJava), cmd)


    //Parse Model
    (ConstList.make((i :: baseModel).asJava), cmd)
  }

  /**
    * Devolve as instâncias que satisfazem o modelo do circuito especificado
    * @param reo Reo
    * @return
    */
  def getSolutions(reo : List[Nodo]) : A4Solution = {
    val opt = new A4Options()
    opt.solver = A4Options.SatSolver.SAT4J

    val model = getAlloy(reo)

    TranslateAlloyToKodkod.execute_command(NOP, model._1, model._2, opt)
  }

  /**
    * Devolve as instâncias que satisfazem o modelo do circuito,
    * para um comando especificado
    * @param reo Reo
    * @param cmd comando a satisfazer, fornecido pelo utilizador
    * @return
    */
  def getSolutions(reo : List[Nodo], cmd : Command) : A4Solution = {
    val opt = new A4Options()
    opt.solver = A4Options.SatSolver.SAT4J

    val i = getInstance(unfoldNode(reo))
    currentInst = i

    val new_cmd = new Command(cmd.check, cmd.overall, cmd.bitwidth, cmd.maxseq, cmd.formula.and(inst_expr))

    //BaseModel
    //TranslateAlloyToKodkod.execute_command(NOP, ConstList.make((i :: baseSigs.values.toList ++ connSigs.values.toList).distinct.asJava),
    //  new_cmd, opt)

    //Parse Model
    TranslateAlloyToKodkod.execute_command(NOP, ConstList.make((i :: baseModel).asJava), new_cmd, opt)
  }

  /**
    * Verifica a propriedade especificada em linguagem Alloy relativamente ao circuito.
    * Retornando contra-exemplos se a propriedade for falsa.
    * @param reo circuito
    * @param prop propriedade
    * @return
    */
  def checkProperty(reo : List[Nodo], prop : String) : A4Solution = {
    val opt = new A4Options()
    opt.solver = A4Options.SatSolver.SAT4J
    val property : Expr = CompUtil.parseOneExpression_fromString(world, "not(" + prop + ")" )

    val (model, cmd_min) = getAlloy(reo)


    val cmd = new Command(true, cmd_min.overall, cmd_min.bitwidth, cmd_min.maxseq, cmd_min.formula.not().and(property))

    //BaseModel
    //TranslateAlloyToKodkod.execute_command(NOP, model, cmd, opt)

    //Parse Model
    TranslateAlloyToKodkod.execute_command(NOP, model, cmd, opt)
  }

  /**
    * Determina o excerto Alloy associado às declarações que dependem do circuito fornecido.
    * O cálculo é efetuado para o último circuito que foi passado ao gerador.
    * Se não tiver sido passado nenhum circuito, é devolvida a string vazia.
    * @return
    */
  private def instanceToString() : String = {

    if(currentInst == NONE)
      return ""

    var fields = ""

    if(currentOneNode.nonEmpty)
      fields += currentOneNode.mkString("\t", ", ", " : one Node,\n")

    if(currentLoneNode.nonEmpty)
      fields += currentLoneNode.mkString("\t", ", ", " : lone Node,\n")

    currentConn.foreach(sig =>
      if(sig._2.nonEmpty)
        fields += sig._2.mkString("\t", ", ", " : one " + connSigs(sig._1).label + ",\n")
    )

    if(fields.length > 2)
      fields = fields.substring(0, fields.length - 2)

    var facts = ""

    currentInst.getFacts.forEach(f => facts += "\t" + f + "\n")

    facts = facts.replaceAll("DISJOINT", "disj")

    s"""
       |one sig Instance{
       |\tports : set Node,
       |\tconns : set Connector,
       |$fields
       |}
       |
      |fun nodes : set Node{
       |\tInstance.ports + Instance.conns.ports + Instance.conns.^conns.ports
       |}
       |
      |fact{
       |\tall c : Connector | c in Instance.conns + Instance.conns.^conns
       |\tall n : Node | n in nodes
       |\tall c : Connector | c not in c.^conns
       |\tall s : State - last | s.fire = s.next.fire and no s.fire implies all n : Node | cantFire[s, n]
       |$facts}
    """.stripMargin
  }

  /**
    * Preenche o Map[String,String] connDecl com (nome,declaração) de cada conector
    * @return
    */
  private def initConnDecl() : Unit ={

    connDecl.put("sync","""
                           |sig Sync extends Channel {}{
                           |  all s: State | e1 in s.fire iff e2 in s.fire
                           |}
                           |
                           |pred sync[src: one Node, s: one Sync, sink: one Node]{
                           |  src = s.e1
                           |  sink = s.e2
                           |}
                         """.stripMargin)

    connDecl.put("drain","""
                           |sig Drain extends Channel {}{
                           |  all s : State | e1 in s.fire iff e2 in s.fire
                           |}
                           |
                           |pred drain[sink1, sink2 : one Node, d : one Drain]{
                           |  sink1 = d.e1
                           |  sink2 = d.e2
                           |}
                         """.stripMargin)

    connDecl.put("lossy","""
                           |sig Lossy extends Channel {}{
                           |  all s : State | e2 in s.fire implies e1 in s.fire
                           |}
                           |
                           |pred lossy[src : one Node, l : one Lossy, sink : one Node]{
                           |  src = l.e1
                           |  sink = l.e2
                           |}
                         """.stripMargin)

    connDecl.put("fifo","""
                           |sig Value{}
                           |
                           |sig Fifo extends Channel { buffer : Value lone -> State }{
                           |  all s : State - first | let ant = s.prev,  received = e1 in ant.fire, sent = e2 in ant.fire,
                           |           emptyBefore = no buffer.ant,  emptyNow = no buffer.s,
                           |           fullBefore = some buffer.ant,  fullNow = some buffer.s
                           |  {
                           |   received implies emptyBefore and fullNow and not sent
                           |
                           |   sent implies fullBefore and emptyNow and not received
                           |
                           |   not received and not sent implies buffer.ant = buffer.s
                           |  }
                           |}
                           |
                           |pred fifo[src : one Node, f : one Fifo, sink: one Node]{
                           |  src = f.e1
                           |  sink = f.e2
                           |
                           |  no f.buffer.first
                           |}
                           |
                           |pred fifofull[src : one Node, f : one Fifo, sink: one Node]{
                           |  src = f.e1
                           |  sink = f.e2
                           |  some  f.buffer.first
                           |}
                         """.stripMargin)

    connDecl.put("merger","""
                           |sig Merger extends Connector {
                           |  disj i1, i2, o : one Node
                           |}
                           |{
                           |  ports = i1 + i2 + o
                           |  conns = none
                           |}
                           |
                           |pred merger[inp1, inp2 : one Node, m : one Merger, out : one Node]{
                           |  inp1 = m.i1
                           |  inp2 = m.i2
                           |  out = m.o
                           |
                           |  all s : State | let fire_i1 = inp1 in s.fire, fire_i2 = inp2 in s.fire  {
                           |    fire_i1 implies not fire_i2
                           |    fire_i2 implies not fire_i1
                           |
                           |    out in s.fire iff fire_i1 or fire_i2
                           |  }
                           |}
                         """.stripMargin)

    connDecl.put("dupl","""
                           |sig Replicator extends Connector {
                           |  disj i, o1, o2 : one Node
                           |}
                           |{
                           |  ports = i + o1 + o2
                           |  conns = none
                           |}
                           |
                           |pred replicator[inp : one Node, r : one Replicator, out1, out2 : one Node]{
                           |  inp = r.i
                           |  out1 = r.o1
                           |  out2 = r.o2
                           |
                           |  all s : State {
                           |    inp in s.fire iff out1 in s.fire
                           |    inp in s.fire iff out2 in s.fire
                           |  }
                           |}
                         """.stripMargin)

    connDecl.put("vmerger","""
                           |sig VMerger extends Connector {
                           |  o : one Node,
                           |  i1, i2 : lone Node
                           |}
                           |{
                           |  disj[o, i1, i2]
                           |  ports = i1 + i2 + o
                           |  conns = none
                           |}
                           |
                           |pred vmerger[inp1, inp2 : lone Node, m : one VMerger, out : one Node]{
                           |  inp1 = m.i1
                           |  inp2 = m.i2
                           |  out = m.o
                           |
                           |  all s : State | let fire_i1 = m.i1 in s.fire, fire_i2 = m.i2 in s.fire  {
                           |    some m.i1 and fire_i1 implies not fire_i2
                           |    some m.i2 and fire_i2 implies not fire_i1
                           |
                           |    some m.i1 + m.i2 implies out in s.fire iff fire_i1 or fire_i2
                           |  }
                           |}
                         """.stripMargin)

    connDecl.put("vdupl","""
                           |sig VDupl extends Connector{
                           |  i : one Node,
                           |  o1, o2 : lone Node
                           |}{
                           |  disj[i, o1, o2]
                           |  ports = i + o1 + o2
                           |  conns = none
                           |}
                           |
                           |pred vdupl[inp : one Node, r : one VDupl, out1, out2 : lone Node]{
                           |  inp = r.i
                           |  out1 = r.o1
                           |  out2 = r.o2
                           |
                           |  all s : State {
                           |    some out1 implies inp in s.fire iff out1 in s.fire
                           |    some out2 implies inp in s.fire iff out2 in s.fire
                           |  }
                           |}
                         """.stripMargin)

  }

  /**
    * Determina a representação em formato .als do modelo Alloy para o último circuito processado.
    * @return
    */
  def modelToString() : String = {
    var model = """|open util/ordering[State] as S
                   |
                   |sig Node {}
                   |
                   |abstract sig Connector {
                   |  conns : set Connector ,
                   |  ports : set Node
                   |}
                   |
                   |abstract sig Channel extends Connector {
                   |  e1, e2 : one Node
                   |}
                   |{
                   |  ports = e1 + e2
                   |  conns = none
                   |}
                   |
                   |sig State{ fire : set Node }
                """.stripMargin

    connInCircuit.foreach(c => model += connDecl(c))

    model += this.instanceToString

    //Adaptar declaração do predicado 'cantFire' para quando no circuito
    //não constam FIFO/Merger/VMerger
    var f = s"""|\t(some f : Fifo | n = f.e1 and some f.buffer.s or\t
                |\t			            n = f.e2 and no f.buffer.s) or\t
            """.stripMargin

    var m = s"""|\t(some m : Merger | n = m.i1 and m.i2 in s.fire or\t
                |\t				            n = m.i2 and m.i1 in s.fire) or\t
            """.stripMargin

    var vm = s"""|\t(some vm : VMerger | n = vm.i1 and some vm.i2 and vm.i2 in s.fire or\t
                 |\t					           n = vm.i2 and some vm.i1 and vm.i1 in s.fire) or\t
              """.stripMargin

    if(!(connInCircuit contains "fifo"))
      f = ""
    if(!(connInCircuit contains "merger"))
      m = ""
    if(!(connInCircuit contains "vmerger"))
      vm = ""

    model +=
      s"""
        |pred cantFire[s : State, n : Node]{
        |$f$m$vm some none
        |}
      """.stripMargin


    model
  }
}
