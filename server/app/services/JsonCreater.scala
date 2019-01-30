package services


import play.api.libs.json._
import preo.ast._
import preo.frontend.Show

/**@
  * Provides the methods to convert objects into json. Used in socket communication
  */
object JsonCreater {

  def create(typ: Type, reductyp:Type, con:CoreConnector): JsValue =
    JsObject(Map(
      "type" -> convert(typ),
      "reducType" -> convert(reductyp),
      "connector" -> convert(con),
      "id" -> JsNumber(Thread.currentThread().getId)
    ))

  def create(output: String): JsValue = {
    JsObject(Map(
      "output" -> JsString(output)
    ))
  }


  def createError(error: String): JsValue = {
    JsObject(Map(
      "error" -> JsString(error))
    )
  }

  private def convert(connector: CoreConnector): JsValue = connector match{
    case CSeq(c1, c2) => {
      JsObject(Map(
        "type" -> JsString("seq"),
        "c1" -> convert(c1),
        "c2" -> convert(c2))
      )
    }
    case CPar(c1, c2) => {
      JsObject(Map(
        "type" -> JsString("par"),
        "c1" -> convert(c1),
        "c2" -> convert(c2)
      ))
    }
    case CId(i) => {
      JsObject(Map(
        "type" -> JsString("id"),
        "i" -> convert(i)
      ))
    }

    case CSymmetry(i,j) => {
      JsObject(Map(
        "type" -> JsString("symmetry"),
        "i" -> convert(i),
        "j" -> convert(j)
      ))
    }
    case CTrace(i,c) => {
      JsObject(Map(
        "type" -> JsString("trace"),
        "i" -> convert(i),
        "c" -> convert(c)
      ))
    }
    case CPrim(name,i,j,extra) => {
      JsObject(Map(
        "type" -> JsString("prim"),
        "name" -> JsString(name),
        "i" -> convert(i),
        "j" -> convert(j),
        "extra" -> JsString(extra.mkString(","))
//          (extra match {
//          case Some(ob) => JsString(ob.toString)
//          case None => JsString("")
//        })
      ))
    }

    case CSubConnector(name, c, ann) => {
      JsObject(Map(
        "type" -> JsString("sub"),
        "name" -> JsString(name),
        "c" -> convert(c),
        "ann" -> convertAnns(ann)
      ))
    }
  }

  private def convert(i: CoreInterface): JsValue = convert(i.toInterface)

  private def convert(i: Interface): JsValue = JsString(Show(i))

  private def convert(t : Type): JsValue = JsString(t.toString)

  private def convertAnns(annotations: List[Annotation]): JsValue = annotations match {
    case Nil => JsObject(Map(
      "type" -> JsString("Nil")
    ))
    case ann::rest => JsObject(Map(
      "type" -> JsString("Some"),
      "head" -> convertAnn(ann),
      "tail" -> convertAnns(rest)
    ))
  }
  private def convertAnn(annotation: Annotation): JsValue = JsObject(Map(
    "name" -> JsString(annotation.name),
    "hasValue" -> JsTrue,
    "value" -> (annotation.value match {
      case None => JsString("")
      case Some(e) => JsString(Show(e))
    })
  ))
}
