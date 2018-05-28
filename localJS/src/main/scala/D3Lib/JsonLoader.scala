package D3Lib

import preo.ast._
import preo.backend._
import scala.util.parsing.json._

object JsonLoader {
  def apply(rawjs: String): Unit = {//} Either[(String, String, CoreConnector, Map[String, List[Any]], Map[String, List[Any]], String), String] = {
    val json = JSON.parseRaw(rawjs)
    println(json)

    print(json.toString)

    println(rawjs)

//      parseFull(rawjs).get.asInstanceOf[Map[String, Any]]
//    if (json.contains("error")) Right(json.get("error").asInstanceOf[String])
//    else
//      Left(json.get("type").asInstanceOf[String], json.get("reducType").asInstanceOf[String],
//        convert(json.get("graph")), convert(json.get("automata")))

  }



}
