package services


import play.api.libs.json._
import preo.ast.{CId, CPar, CPrim, CSeq, CSubConnector, CSymmetry, CTrace, CoreConnector}
import preo.frontend.Show

/**@
  * Provides the methods to convert objects into json. Used in socket communication
  */
object JsonCreater {

  def create(con:CoreConnector): JsValue = {
    convertConnector(con)
  }

  private def convertConnector(connector: CoreConnector): JsValue = connector match{
    case CSeq(c1, c2) => {
      JsObject(Seq(
        "type" -> JsString("seq"),
        "c1" -> convertConnector(c1),
        "c2" -> convertConnector(c2)
      ))
    }
    case CPar(c1, c2) => {
      JsObject(Seq(
        "type" -> JsString("par"),
        "c1" -> convertConnector(c1),
        "c2" -> convertConnector(c2)
      ))
    }
    case CId(i) => {
      JsObject(Seq(
        "type" -> JsString("id"),
        "i" -> JsString(Show(i.toInterface))
      ))
    }

    case CSymmetry(i,j) => {
      JsObject(Seq(
        "type" -> JsString("id"),
        "i" -> JsString(Show(i.toInterface)),
        "j" -> JsString(Show(j.toInterface))
      ))
    }
    case CTrace(i,c) => {
      JsObject(Seq(
        "type" -> JsString("id"),
        "i" -> JsString(Show(i.toInterface)),
        "c" -> convertConnector(c)
      ))
    }
    case CPrim(name,i,j,extra) => {
      JsObject(Seq(
        "type" -> JsString("prim"),
        "name" -> JsString(name),
        "i" -> JsString(Show(i.toInterface)),
        "j" -> JsString(Show(j.toInterface)),
      ))
    }

    case CSubConnector(name, c) => {
      JsObject(Seq(
        "type" -> JsString("sub"),
        "name" -> JsString(name),
        "i" -> convertConnector(c)
      ))
    }
  }

  private def convertType(t : Type)
}
