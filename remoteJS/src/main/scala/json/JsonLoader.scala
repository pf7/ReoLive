package json

import preo.ast._

import scala.util.parsing.json._

object Loader{
  def apply(rawjs: String):  Either[(String, String, CoreConnector, Int), String] = {
    println(rawjs)
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

  private def convertCon(raw: Map[String, Any]): CoreConnector = {

    raw("type").asInstanceOf[String] match {
      case x if x == "seq" => CSeq(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case x if x == "par" => CPar(convertCon(raw("c1").asInstanceOf[Map[String, Any]]), convertCon(raw("c2").asInstanceOf[Map[String, Any]]))
      case x if x == "id" => CId(convertInterface(raw("i").asInstanceOf[String]))
      case x if x == "symmetry" => CSymmetry(convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]))
      case x if x == "trace" => CTrace(convertInterface(raw("i").asInstanceOf[String]), convertCon(raw("c").asInstanceOf[Map[String, Any]]))
      case x if x == "prim" => CPrim(raw("name").asInstanceOf[String], convertInterface(raw("i").asInstanceOf[String]), convertInterface(raw("j").asInstanceOf[String]), None)
      case x if x == "sub" => CSubConnector(raw("name").asInstanceOf[String], convertCon(raw("c").asInstanceOf[Map[String, Any]]), Nil)
      case _ => null
    }
  }

  private def convertInterface(i: String): CoreInterface = CoreInterface(i.toInt)

  def loadModalOutput(msg: String): Either[String, String] = {
    val parsed = JSON.parseFull(msg).get.asInstanceOf[Map[String, Any]]

    if (parsed.contains("error")) {
      Right(parsed("error").asInstanceOf[String])
    }
    else{
      Left(parsed("output").asInstanceOf[String])
    }
  }
}

