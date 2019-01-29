package common.widgets.virtuoso
import preo.DSL._
import preo.ast.{BExpr, Connector, Prim, SubConnector}
import preo.examples.Repository
import preo.lang.Parser

object VirtuosoParser {
  def virtParser:VirtuosoParser = new VirtuosoParser {}

  type Result[T] = Either[String,T]

  def parse(c:String): Result[Connector] = virtParser.parse(c)
}


trait VirtuosoParser extends preo.lang.Parser {

  override def inferPrim(s: String): Connector = s match {
    case "data"     => Prim(s,1,1)
    case "dataEvent"=> Prim(s,1,1)
    case "blackboard"=> Prim(s,1,1)
    case "semaphore"=> Prim(s,1,1)
    case "resource" => Prim(s,1,0)
    case "dupl"     => Prim("dupl",1,2,Some("all"))
    case "xor"      => Prim("dupl",1,2,Some("one"))
    case "node"     => SubConnector(s,Repository.nodeGen(Prim("dupl",1,2,Some("one"))), Nil)
    case "dupls"    => SubConnector(s,Repository.duplsGen(Prim("dupl",1,2,Some("all"))), Nil)
    case "xors"     => SubConnector(s,Repository.duplsGen(Prim("dupl",1,2,Some("one"))), Nil)
    ///////
    case "fifo"     => fifo
    case "fifofull" => fifofull
    case "drain"    => drain
    case "id"       => id
    case "ids"      => lam(n, id ^ n)
    case "lossy"    => lossy
    case "merger"   => merger
    case "swap"     => swap
    case "noSrc"    => Prim("noSrc",1,0)
    case "noSnk"    => Prim("noSnk",0,1)
    case "writer"   => Prim("writer",0,1,Some("component"))
    case "reader"   => Prim("reader",1,0,Some("component"))
    case "mergers"  => SubConnector(s,Repository.mergers, Nil)
    case "zip"      => SubConnector(s,Repository.zip, Nil)
    case "unzip"    => SubConnector(s,Repository.unzip, Nil)
    case "exrouter" => SubConnector(s,Repository.exrouter, Nil)
    case "exrouters"=> SubConnector(s,Repository.nexrouter, Nil)
    case "fifoloop" => SubConnector(s,Repository.fifoloop, Nil)
    case "sequencer"=> SubConnector(s,Repository.sequencer, Nil)
    case "barrier"  => SubConnector(s,Repository.barrier, Nil)
    case "barriers" => SubConnector(s,Repository.barriers, Nil)
  }
}
