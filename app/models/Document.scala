package models

import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._

case class Document(id: Long, title: String, body: String) {
  def hashtags = {
    """#(\w+)""".r.findAllIn(body).matchData.map(_.group(1).toLowerCase).toList
  }
}

object Document {
  implicit val documentWrites: Writes[Document] = (
      (JsPath \ "id").write[Long] and
      (JsPath \ "title").write[String] and
      (JsPath \ "body").write[String] and
      (JsPath \ "hashtags").write[List[String]]
    )((d: Document) => (d.id, d.title, d.body, d.hashtags))

  //implicit val documentFormat = Json.format[Document]
}