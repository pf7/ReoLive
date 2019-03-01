package common.widgets.virtuoso
import preo.DSL._
import preo.ast.{BExpr, Connector, Prim, SubConnector}
import preo.examples.Repository
import preo.frontend.TreoLite
import preo.lang.Parser

object VirtuosoParser {
  def virtParser:VirtuosoParser = new VirtuosoParser {}

  type Result[T] = Either[String,T]

  def parse(c:String): Result[Connector] = virtParser.parse(c)

  val PRIMITIVE = Set(
      "semaphore"
    , "resource"
    , "port"
    , "dataEvent"
    , "event"
    , "fifo"
    , "blackboard"
    , "node"
    , "dupl"
    , "dupls"
    , "xor"
    , "xors"
    , "mrg"
    , "drain"
  )
}


trait VirtuosoParser extends preo.lang.Parser {

  override def preo: Parser[Connector] =
    prog ^^ {p => TreoLite.expand(p,inferPrim,"xor")}


  override def inferPrim(s: String): Connector = s match {
    case "dupl"     => Prim("dupl",1,2)
    case "xor"      => Prim("xor",1,2)
    case "node"     => SubConnector(s,Repository.nodeGen(Prim("dupl",1,2)), Nil)
    case "dupls"    => SubConnector(s,Repository.duplsGen(Prim("dupl",1,2)), Nil)
    case "xors"     => SubConnector(s,Repository.duplsGen(Prim("xor",1,2)), Nil)
    case "resource" => Prim(s,2,0)
    // // not needed (default case)
    //    case "port"     => Prim(s,1,1)
    //    case "event"     => Prim(s,1,1)
    //    case "dataEvent"=> Prim(s,1,1)
    //    case "blackboard"=> Prim(s,1,1)
    //    case "semaphore"=> Prim(s,1,1)
    ///////
    // // not needed (in super.inferPrim)
    case _ => super.inferPrim(s)
//    case "fifo"     => fifo
//    case "fifofull" => fifofull
//    case "drain"    => drain
//    case "id"       => id
//    case "ids"      => lam(n, id ^ n)
//    case "lossy"    => lossy
//    case "merger"   => merger
//    case "swap"     => swap
//    case "noSrc"    => Prim("noSrc",1,0)
//    case "noSnk"    => Prim("noSnk",0,1)
//    case "writer"   => Prim("writer",0,1,Set("component"))
//    case "reader"   => Prim("reader",1,0,Set("component"))
//    case "mergers"  => SubConnector(s,Repository.mergers, Nil)
//    case "zip"      => SubConnector(s,Repository.zip, Nil)
//    case "unzip"    => SubConnector(s,Repository.unzip, Nil)
//    case "exrouter" => SubConnector(s,Repository.exrouter, Nil)
//    case "exrouters"=> SubConnector(s,Repository.nexrouter, Nil)
//    case "fifoloop" => SubConnector(s,Repository.fifoloop, Nil)
//    case "sequencer"=> SubConnector(s,Repository.sequencer, Nil)
//    case "barrier"  => SubConnector(s,Repository.barrier, Nil)
//    case "barriers" => SubConnector(s,Repository.barriers, Nil)
//
//    case _ => Prim(s,1,1)
  }
}
