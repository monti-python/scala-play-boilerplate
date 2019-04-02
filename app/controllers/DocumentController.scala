package controllers

import akka.actor.Status.Success
import javax.inject.Inject
import models.DocumentRepository
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext

class DocumentController @Inject()(cc: ControllerComponents, documentRepository: DocumentRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index = Action.async { implicit request =>
    documentRepository.list().map(documents => Ok(Json.toJson(documents)))
  }

  def getById(id: Long) = Action.async { implicit request =>
    documentRepository.getById(id).map(document => Ok(Json.toJson(document)))
  }

  def createDocument = Action.async(parse.json) { implicit request: Request[JsValue] =>
    documentRepository.create(
      (request.body \ "title").toString,
      (request.body \ "body").toString
    ).map(document => Created(Json.toJson(document)))
  }

  def testz = Action.async { implicit request =>
    documentRepository.list().map(documents => Ok(Json.toJson(documents)))
  }
}
