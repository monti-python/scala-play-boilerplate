package controllers

import javax.inject.Inject
import models.DocumentRepository
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class DocumentController @Inject()(cc: ControllerComponents, documentRepository: DocumentRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  def index = Action.async { implicit request =>
    documentRepository.list().map(documents => Ok(Json.toJson(documents)))
  }
}
