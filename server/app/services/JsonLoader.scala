package services

import play.api.libs.json.{JsDefined, JsString, JsValue, Json}

/**@
  * Provides the methods to convert json into objects. Used in socket communication
  * Maybe useless
  */
object JsonLoader {
  def parse(msg: String): (Option[String], Option[String]) = {
    val result: JsValue = Json.parse(msg)
    val raw_connector = result\"connector"
    val connector = raw_connector match{
      case JsDefined(x) => Some(x.asInstanceOf[JsString].value)
      case undefined => None
    }
    val raw_modal = result\"modal"
    val modal = raw_modal match{
      case JsDefined(x) => Some(x.asInstanceOf[JsString].value)
      case undefined => None
    }
    (connector, modal)
  }
}
