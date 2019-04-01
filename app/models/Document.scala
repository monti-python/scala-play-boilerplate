package models

import play.api.libs.json._

case class Document(id: Long, title: String, body: String)

object Document {
  implicit val documentFormat = Json.format[Document]
}