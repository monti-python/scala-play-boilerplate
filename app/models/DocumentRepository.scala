package models

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ Future, ExecutionContext }

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

@Singleton
class DocumentRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class DocumentTable(tag: Tag) extends Table[Document](tag, "documents") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def body = column[String]("body")
    def * = (id, title, body) <> ((Document.apply _).tupled, Document.unapply)
  }

  private val documents = TableQuery[DocumentTable]

  def list(): Future[Seq[Document]] = db.run {
    documents.result
  }

  def getById(id: Long) = db.run {
    documents.filter(_.id === id).result.head
  }

  def create(title: String, body: String): Future[Document] = db.run {
    (
      documents.map(p => (p.title, p.body))
      returning documents.map(p => p.id)
      into ((titleBody, id) => Document(id, titleBody._1, titleBody._2))
     ) += (title, body)
  }

  def deleteAll() = db.run{
    documents.delete
  }

  def resetAutoInc() = db.run {
    sqlu"ALTER TABLE documents AUTO_INCREMENT=1;"
  }
}
