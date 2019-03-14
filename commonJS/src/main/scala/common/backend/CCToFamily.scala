package common.backend


import ifta._
import ifta.analyse.Simplify
import ifta.common.FExpOverflowException
import ifta.reo.Connectors._
import preo.ast.{CPrim, CoreConnector}
import preo.backend.Network
import preo.common.GenerationException

/**
  * Created by guille on 11/12/2018
  */


object CCToFamily {

//  def cc2NReoIFTA(cc:CoreConnector): NReoIFTA = {
//    val reoGraph = ReoGraph.toGraphOneToOne(cc,hideClosed = false)
//    NReoIFTA(reoGraph.edges.map(e => ReoIFTA(edgeToIFTA(e),e)).toSet)
//  }

  def toRifta(cc:CoreConnector):NReoIFTA = {
    val reoGraph = Network.toGraphWithoutSimplification(cc,hideClosed = false)
    buildNIFTA(reoGraph)
  }

  def toIFTA(cc:CoreConnector,showFullName:Boolean,hideInternal:Boolean):IFTA ={
    val reoGraph = Network.toGraphWithoutSimplification(cc,hideClosed = false)
    buildNIFTA(reoGraph).getReoIFTA(showFullName,hideInternal)
  }

  private def buildNIFTA(reoGraph: Network): NReoIFTA = {

    // to build automata in a more efficient way, i.e.
    // composing always primitive connectors that share ports
    // so that the automata doesn't grow to quickly
    val (ins,outs) = Network.collectInsOuts(reoGraph)
    def getNeighbours(e:Network.Prim): List[Network.Prim] =
      (for (i <- e.ins)  yield outs.getOrElse(i,Set())).flatten ++
        (for (o <- e.outs) yield ins.getOrElse(o,Set())).flatten

    if (reoGraph.prims.nonEmpty){
      var currentEdge = reoGraph.prims.head
      var restEdges = reoGraph.prims.toSet - currentEdge

      //      var nifta = NIFTA(Set(edgeToIFTA(currentEdge)))
      var nReoIFTA:NReoIFTA = NReoIFTA(Set(ReoIFTA(edgeToIFTA(currentEdge),currentEdge)))
      var nextEdges = getNeighbours(currentEdge)

      while(restEdges.nonEmpty){
        while (nextEdges.nonEmpty) {
          currentEdge = nextEdges.head
          nextEdges = nextEdges.tail
          //          nifta = nifta || edgeToIFTA(currentEdge)
          nReoIFTA = NReoIFTA(nReoIFTA.reoIFTAs ++ Set(ReoIFTA(edgeToIFTA(currentEdge),currentEdge)))
          restEdges -= currentEdge
        }
        if (restEdges.nonEmpty) {
          currentEdge = restEdges.head
          restEdges = restEdges.tail
          //          nifta = nifta || edgeToIFTA(currentEdge)
          nReoIFTA = NReoIFTA(nReoIFTA.reoIFTAs ++ Set(ReoIFTA(edgeToIFTA(currentEdge),currentEdge)))
          nextEdges = getNeighbours(currentEdge)
        }
      }
      //      nifta
      nReoIFTA
    }
    else
      NReoIFTA(Set())
  }


  private def edgeToIFTA(e: Network.Prim):IFTA = e match {
    case Network.Prim(CPrim("sync",_,_,_),List(a),List(b),_) =>
      sync(a.toString,b.toString) name "sync"
    case Network.Prim(CPrim("id",_,_,_),List(a),List(b),_) =>
      sync(a.toString,b.toString) name "id" // create something different later
    case Network.Prim(CPrim("lossy", _, _, _), List(a), List(b),_) =>
      lossy(a.toString,b.toString) name "lossy"
    case Network.Prim(CPrim("fifo",_,_,_),List(a),List(b),_) =>
      fifo(a.toString,b.toString) name "fifo"
    case Network.Prim(CPrim("fifofull", _, _, _), List(a), List(b),_) =>
      fifofull(a.toString,b.toString) name "fifofull"
    case Network.Prim(CPrim("drain", _, _, _), List(a, b), List(),_) =>
      sdrain(a.toString,b.toString) name "drain"
    case Network.Prim(CPrim("merger", _, _, _), List(a, b), List(c),_) =>
      vmerger(a.toString,b.toString,c.toString) name "merger"
    case Network.Prim(CPrim("dupl", _, _, _), List(a), List(b, c),_) =>
      vrepl(a.toString,b.toString,c.toString) name "dupl"
    case Network.Prim(CPrim("writer", _, _, _), List(), List(a),_) =>
      writer(a.toString)
    case Network.Prim(CPrim("reader", _, _, _), List(a), List(),_) =>
      reader(a.toString)
    case Network.Prim(CPrim("noSnk", _, _, _), List(), List(a),_) =>
      noSink(a.toString)
    case Network.Prim(CPrim("noSrc", _, _, _), List(a), List(),_) =>
      noSrc(a.toString)

    // unknown name with type 1->1 -- behave as identity
    case Network.Prim(CPrim(name, _, _, _), List(a), List(b),_) =>
      sync(a.toString,b.toString) name name

    case Network.Prim(p, _, _,_) =>
      throw new GenerationException(s"Unknown ifta automata for primitive $p")
  }
}


case class ReoIFTA(ifta:IFTA, edge:Network.Prim) {

  lazy val conName = getName

  // oldName -> (conName, actIndex, dir)
  lazy val actsNames:Map[String,(String,String,String)] = {
    val inIndexes:List[(String,String)] =
      edge.ins.map(_.toString).zip ((Stream from 1).map(_.toString))
    val outIndexes:List[(String,String)] =
      edge.outs.map(_.toString).zip ((Stream from 1).map(_.toString))

    inIndexes.foldLeft(Map[String,(String,String,String)]()){
      (m,i) => m + (i._1 -> (getName, if(inIndexes.size ==1) "" else i._2, "↓"))} ++
    outIndexes.foldLeft(Map[String,(String,String,String)]()){
      (m,o) => m + (o._1 -> (getName, if(outIndexes.size == 1) "" else o._2, "↑"))}
  }

  //not necesary for now
  // oldFeatName -> (correspondingConName,actIndex,dir)
  lazy val featsNames:Map[String,(String,String,String)] = {
    var res:Map[String,(String,String,String)] = Map()
    for (f <- ifta.feats) {
      var actName = f.slice(2,f.size) // remove v_ to get act name of the feature
      var n = actsNames.getOrElse(f,(f,"",""))
      res += f -> (s"f${n._1}", n._2,n._3)
    }
    res
  }

  private def getName:String = edge.parents match {
    case Nil     => primName(edge.prim)
    case ""::_   => primName(edge.prim)
    case head::_ => head
  }

//  private def primName(prim: CPrim): String = (prim.name,prim.extra) match {
//    case ("writer",Some(s:String)) => s"wr($s)"
//    case ("reader",Some(s:String)) => s"rd($s)"
//    case (n,Some(s:String)) => s"$n($s)"
//    case (n,_) => n
//  }
  private def primName(prim: CPrim): String = (prim.name,prim.extra.toList) match {
    case ("writer",List(s:String)) => s"wr($s)"
    case ("reader",List(s:String)) => s"rd($s)"
    case (n,List(s:String)) => s"$n($s)"
    case (n,_) => n
  }
}

case class NReoIFTA(reoIFTAs:Set[ReoIFTA]) {

  private var ifta:IFTA = _

  private lazy val iftaSimple = // try {
    NIFTA(reoIFTAs.map(_.ifta)).flatten(30000)
//  } catch {
//    case e:Throwable => throw new RuntimeException("exception at flatten")
//  }

  private lazy val iftaHiden = //try {
    NIFTA(reoIFTAs.map(_.ifta)).hideFlatten(30000)
//  } catch {
//    case e:Throwable => throw new RuntimeException("exception at hideFlatten" + "\n" + e.getMessage)
//  }

  def getNifta:NIFTA = NIFTA(reoIFTAs.map(_.ifta))

  def getIFTA(hideIntenal:Boolean) = if (hideIntenal) iftaHiden else iftaSimple

  def getFm = NIFTA(reoIFTAs.map(_.ifta)).fm

  def getFeats:Set[String] = getNifta.iFTAs.map(_.feats).flatten

  def getLocs:Set[Int] = ifta.locs

  def getInit:Int = ifta.init

  def getReoIFTA(allNames:Boolean,hideInternal: Boolean): IFTA = {
    if (hideInternal) ifta = iftaHiden else ifta = iftaSimple
    if (allNames)  actNames = actNamesFull else actNames = actNamesSimple
    IFTA(ifta.locs,ifta.init,mkNewActs(ifta.act),ifta.clocks
      ,ifta.feats //mkNewFeats(ifta.feats)
      ,ifta.edges.map(mkNewEdge(_,hideInternal)),ifta.cInv,ifta.fm//mkNewFE(ifta.fm)
      ,mkNewActs(ifta.in),mkNewActs(ifta.out),ifta.aps,ifta.shortname)
  }

  // (oldAct, Set((conName,IndexOfInOrOut,InOrOut))
  private var actNames:Map[String, Set[(String,String,String)]] = _

  private lazy val actNamesSimple:Map[String, Set[(String,String,String)]] = {
    reoIFTAs.map(_.actsNames.toSeq)
      .foldRight(Seq[(String,(String,String,String))]())(_++_)
      .groupBy(_._1)
      .mapValues(_.map(_._2).toSet)
  }

  private lazy val actNamesFull:Map[String, Set[(String,String,String)]] = {
    var consByName:Map[String, Set[ReoIFTA]] = reoIFTAs.groupBy(_.conName)
    var res:Map[String,Set[(String,String,String)]] = Map()
    var temp:Seq[(String,(String,String,String))] = Seq()
    for (con <- consByName)
      if (con._2.size <= 1)
        temp = temp ++ con._2.map(_.actsNames.toSeq).
          foldRight(Seq[(String,(String,String,String))]())(_++_)
      else {
        var mapIndex = con._2 zip ((Stream from 1).map(_.toString))
        for ((r,i) <- mapIndex)
          temp = temp ++ (r.actsNames.mapValues(a => (a._1+i,a._2,a._3))).toSeq
      }
    temp.groupBy(_._1).mapValues(_.map(_._2).toSet)
  }


  private def mkNewEdge(e:Edge,hideInternal:Boolean):Edge = {
    Edge(e.from,e.cCons,
      if (hideInternal) mkNewActs(e.act.intersect(ifta.interface)) else mkNewActs(e.act)
      ,e.cReset,e.fe,e.to)//mkNewFE(e.fe),e.to)
  }

  private def mkNewActs(act: Set[String]): Set[String] = {
    var acts = (for (a <- act) yield actNames.getOrElse(a,Set())).flatten
    getCleanNames(acts)
  }

  private def getCleanNames(acts: Set[(String, String, String)]): Set[String] = {
    var newNames:Set[String] = Set()
    var byName = acts.groupBy(_._1)
    for ((n,ns) <- byName) {
      var byDir = ns.groupBy(_._3)
      var indIn:String = ""
      var indOut:String = ""
      for ((d, ds) <- byDir) d match{
        case "↓" => indIn = ds.map(_._2).mkString(",")
        case "↑" => indOut = ds.map(_._2).mkString(",")
      }
      if (byDir.size>1)
        newNames = newNames + (n+mkInOut(indIn,indOut))
      else byDir.head._1 match {
        case "↓" => newNames = newNames + (n+mkIn(indIn))
        case "↑" => newNames = newNames + (n+mkOut(indOut))
      }
    }
    newNames
  }

  private def mkNewFE(fe: FExp): FExp = {
    var mapFE:Map[String,Set[String]] = Map()
    var feats = fe.feats.map(f => f.slice(2,f.size))
    mapFE = actNames.map(a => a._1 -> a._2.map(mkActName(_))).filter(m => feats.contains(m._1))
    Simplify(replaceFE(fe,mapFE))
  }

  private def mkNewFeats(feats:Set[String]):Set[String] = {
    var mapFeats:Map[String,Set[String]] = Map()
    var featNames = feats.map(f => f.slice(2,f.size))
    mapFeats = actNames.map(a => a._1 -> a._2.map(mkActName(_))).filter(m => feats.contains(m._1))

    mapFeats.map(_._2).flatten.map(f => s"f$f").toSet
  }

  private def replaceFE(fe:FExp,map:Map[String,Set[String]]):FExp = fe match {
    case Feat(n)      => map.getOrElse(n.slice(2,n.size),Set()).map(f => Feat(s"f$f")).foldLeft[FExp](FTrue)(FAnd(_,_))
    case FTrue        => FTrue
    case FAnd(e1, e2) => replaceFE(e1,map) && replaceFE(e2,map)
    case FOr(e1, e2)  => replaceFE(e1,map) || replaceFE(e2,map)
    case FNot(e)      => FNot(replaceFE(e,map))
    case FImp(e1,e2)  => replaceFE(e1,map) --> replaceFE(e2,map)
    case FEq(e1,e2)   => replaceFE(e1,map) <-> replaceFE(e2,map)
    }

  private def mkActName(act:(String,String,String)):String = act._3 match {
    case "↓" => act._1 + mkIn(act._2) //+ act._3
    case "↑" => act._1 + mkOut(act._2) //+ act._3
  }

  private def mkIn(s:String):String = s"↓$s"
  //s"""↓<tspan baseline-shift="sub">$s</tspan>"""

  private def mkOut(s:String):String = s"↑$s"
  //    s"<p>↑<sup>$s</sup></p>"

  private def mkInOut(in:String,out:String):String =
    if (in.isEmpty && out.isEmpty) "↕" else s"↕${in}_${out}"
  //    s"<p>↕<sup>$out</sup><sub style='position: relative; left: -.5em;'>$in</sub></p>"


}


