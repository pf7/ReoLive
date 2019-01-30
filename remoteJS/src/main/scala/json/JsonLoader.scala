package json

//import play.api.libs.json.{JsDefined, JsString, JsValue, Json}
import preo.ast._

import scala.util.parsing.json._

object Loader{
  def apply(rawjs: String):  Either[(String, String, CoreConnector, Int), String] = {

//    val parsed = Json.parse(rawjs)
//
//    val parseError = parsed \ "error"
//    parseError match {
//      case JsDefined(res) =>
//        println("parsed error - "+res.toString())
//        Right(res.toString())
//      case _ =>
//        println("parsed conn with ")
//        val typ = parsed("type").as[String]
//        val reducTyp = parsed("reducType").as[String]
//        val con = convertCon(parsed("connector"))
//        val id = parsed("id").as[Int]
//        Left((typ, reducTyp, con, id))
//    }


    // println(rawjs)
    val json = JSON.parseRaw(rawjs).get.asInstanceOf[JSONObject]
    val parsed = JSON.parseFull(rawjs).get.asInstanceOf[Map[String, Any]]
    //    println(json.getClass)
    //    json.
    //    print(json.toString)
    //
    //    println(rawjs)
    //
    //    println(parsed)

    if (parsed.contains("error")) {
      Right(parsed("error").asInstanceOf[String])
    }
    else {
      val typ = parsed("type").asInstanceOf[String]
      val reducTyp = parsed("reducType").asInstanceOf[String]
      val con = convertCon(parsed("connector").asInstanceOf[Map[String, Any]])
      val id = parsed("id").asInstanceOf[Int]
      Left((typ, reducTyp, con, id))
    }
  }

  private def convertCon(raw:Map[String,Any]): CoreConnector = {

//    raw("type").as[String] match {
//      case "seq" => CSeq(convertCon(raw("c1")), convertCon(raw("c2")))
//      case "par" => CPar(convertCon(raw("c1")), convertCon(raw("c2")))
//      case "id" => CId(convertInterface(raw("i").as[String]))
//      case "symmetry" => CSymmetry(convertInterface(raw("i").as[String]), convertInterface(raw("j").as[String]))
//      case "trace" => CTrace(convertInterface(raw("i").as[String]), convertCon(raw("c")))
//      case "prim" => CPrim(raw("name").as[String], convertInterface(raw("i").as[String]), convertInterface(raw("j").as[String]), None)
//      case "sub" => CSubConnector(raw("name").as[String], convertCon(raw("c")), Nil)
//      case _ => null
//    }
    raw("type").asInstanceOf[String] match {
      case "seq" => CSeq(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case "par" => CPar(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case "id" => CId(convertInterface(raw("i").asInstanceOf[String]))
      case "symmetry" => CSymmetry(convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]))
      case "trace" => CTrace(convertInterface(raw("i").asInstanceOf[String]), convertCon(raw("c").asInstanceOf[Map[String, Any]]))
      case "prim" => CPrim(raw("name").asInstanceOf[String], convertInterface(raw("i").asInstanceOf[String]),
          convertInterface(raw("j").asInstanceOf[String]), convertSet(raw("extra").asInstanceOf[String]))
      case "sub" => CSubConnector(raw("name").asInstanceOf[String], convertCon(raw("c").asInstanceOf[Map[String, Any]]), convertAnns(raw("ann").asInstanceOf[Map[String,Any]]))
      case _ => null
    }

  }

  private def convertInterface(i: String): CoreInterface = CoreInterface(i.toInt)
  private def convertSome(s:String): Option[String] = if (s.isEmpty) None else Some(s)
  private def convertSet(s:String): Set[Any] = if (s.isEmpty) Set() else Set(s)
  private def convertAnns(map: Map[String, Any]): List[Annotation] = map("type") match {
    case "Nil" => Nil
    case _ => convertAnn(map("head").asInstanceOf[Map[String,Any]]) :: convertAnns(map("tail").asInstanceOf[Map[String,Any]])
  }
  private def convertAnn(map: Map[String, Any]): Annotation = Annotation(map("name").asInstanceOf[String],None) // IGNORING VALUES


  def loadModalOutput(msg: String): Either[String, String] = {
//    val js = Json.parse(msg)
//    js \ "error" match {
//      case JsDefined(err) => Right(err.as[String])
//      case _              => Left(js("output").as[String])
//    }
    val parsed = JSON.parseFull(msg).get.asInstanceOf[Map[String, Any]]

    if (parsed.contains("error")) {
      Right(parsed("error").asInstanceOf[String])
    }
    else{
      Left(parsed("output").asInstanceOf[String])
    }
  }
}

