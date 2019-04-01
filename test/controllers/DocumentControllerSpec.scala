package controllers

import java.sql.Date

import models.DocumentRepository
import org.scalatest._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration._

/*
 * A document may be implemented as follows:
 *
 * case class Document(id: Int, created_at: Date, title: String, body: string) {
 *   def hashtags() = {
 *     // extract hashtags from attribute body
 *   }
 * }
 */
class DocumentControllerSpec extends PlaySpec
                                with BeforeAndAfterEach
                                with GuiceOneAppPerTest
                                with Injecting {
  val injector = new GuiceApplicationBuilder()
    .in(Mode.Test)
    .build()
    .injector
  val documentRepository = injector.instanceOf[DocumentRepository]

  override def beforeEach() {
    Await.ready(
      documentRepository.create(
      "Quarkus, a Kubernetes Native Java Framework",
     "Red Hat has released #Quarkus, a #Kubernetes native #Java framework tailored for GraalVM and OpenJDK HotSpot."
      ),
      10 seconds
   )
    Await.ready(
      documentRepository.create(
        "Java 11 Released",
       "#Java 11 has arrived. The new release is the first planned appearance of #Oracle's #LTS releases, although #Oracle has also grandfathered in Java 8 as an LTS release to help bridge the gap between the old release model and the new approach."
      ),
      10 seconds
    )
  /*
   * Note that you typically do not need to provide an id when creating database records through an ORM or any other access layer.
   * create Document(id = 1, title = "Quarkus, a Kubernetes Native Java Framework", body = "Red Hat has released #Quarkus, a #Kubernetes native #Java framework tailored for GraalVM and OpenJDK HotSpot.")
   * create Document(id = 2, title = "Java 11 Released", body = "#Java 11 has arrived. The new release is the first planned appearance of #Oracle's #LTS releases, although #Oracle has also grandfathered in Java 8 as an LTS release to help bridge the gap between the old release model and the new approach.")
   */
  }

  "GET /documents" should {
    "return HTTP status OK (200)" in {
      val result = route(app, FakeRequest(GET, "/documents")).get
      status(result) mustBe OK
    }

    "return JSON" in {
      val result = route(app, FakeRequest(GET, "/documents")).get
      contentType(result) mustBe Some("application/json")
    }

    "return all documents" in {
      val result = route(app, FakeRequest(GET, "/documents")).get
      val responseBody = contentAsString(result)

      responseBody must include("Quarkus, a Kubernetes Native Java Framework")
      responseBody must include("Java 11 Released")
      responseBody mustNot include("Swift 5 Now Officially Available")
    }
  }

  "GET /documents/:id" should {
    "return OK" in {
      val result = route(app, FakeRequest(GET, "/documents/1")).get
      status(result) mustBe OK
    }

    "return JSON" in {
      val result = route(app, FakeRequest(GET, "/documents/1")).get
      contentType(result) mustBe Some("application/json")
    }

    "return only the document requested" in {
      val result = route(app, FakeRequest(GET, "/documents/1")).get
      val responseBody = contentAsString(result)

      responseBody must include("Quarkus, a Kubernetes Native Java Framework")
      responseBody mustNot include("Java 11 Released")
    }

    "return all extracted hashtags transformed to lowercase" in {
      val result = route(app, FakeRequest(GET, "/documents/1")).get
      val responseBody = contentAsString(result)

      responseBody must include(""""hashtags": ["quarkus", "kubernetes", "java"]""")
    }
  }

  "POST /documents with a valid document" should {
    "return HTTP status CREATED (201)" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey",
          |  "body":  "After having seen and evidenced the tangible benefit of #lean at Mashreq Bank, #agile was seen as a natural progression, an evolutionary step."
          |}"""
        )).get
      status(result) mustBe CREATED
    }

    "return JSON" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey",
          |  "body":  "After having seen and evidenced the tangible benefit of #lean at Mashreq Bank, #agile was seen as a natural progression, an evolutionary step."
          |}"""
        )).get
      contentType(result) mustBe Some("application/json")
    }

    "return the new document" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey",
          |  "body":  "After having seen and evidenced the tangible benefit of #lean at Mashreq Bank, #agile was seen as a natural progression, an evolutionary step."
          |}"""
        )).get
      contentAsString(result) must include("Mashreq Bank’s Lean Agile Journey")
    }

    "extract hashtags" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey",
          |  "body":  "After having seen and evidenced the tangible benefit of #lean at Mashreq Bank, #agile was seen as a natural progression, an evolutionary step."
          |}"""
        )).get
      contentAsString(result) must include(""""hashtags": ["lean", "agile"]""")
    }

    "add the document to the database" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey",
          |  "body":  "After having seen and evidenced the tangible benefit of #lean at Mashreq Bank, #agile was seen as a natural progression, an evolutionary step."
          |}"""
        )).get
      status(result) mustBe CREATED
      val indexResult = route(app, FakeRequest(GET, "/documents")).get
      contentAsString(indexResult) must include("Mashreq Bank’s Lean Agile Journey")
    }
  }

  "POST /documents with an invalid document" should {
    "return HTTP status BAD REQUEST (400)" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey"
          |}"""
        )).get
      status(result) mustBe BAD_REQUEST
    }

    "not add the document to the database" in {
      val result = route(
        app,
        FakeRequest(
          POST,
          "/documents",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Mashreq Bank’s Lean Agile Journey"
          |}"""
        )).get
      status(result) mustBe BAD_REQUEST
      val indexResult = route(app, FakeRequest(GET, "/documents")).get
      contentAsString(indexResult) mustNot include("Mashreq Bank’s Lean Agile Journey")
    }
  }

  "PUT /documents/:id with a valid document" should {
    "return HTTP status ACCEPTED (202)" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework",
          |  "body":  "Quarkus is fast and simply #awesome."
          |}"""
        )).get
      status(result) mustBe ACCEPTED
    }

    "return JSON" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework",
          |  "body":  "Quarkus is fast and simply #awesome."
          |}"""
        )).get
      contentType(result) mustBe Some("application/json")
    }

    "return the updated document" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework",
          |  "body":  "Quarkus is fast and simply #awesome."
          |}"""
        )).get
      contentAsString(result) must include("Quarkus, an awesome Kubernetes Native Java Framework")
    }

    "change the document in the database" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework",
          |  "body":  "Quarkus is fast and simply #awesome."
          |}"""
        )).get
      status(result) mustBe ACCEPTED

      val indexResult = route(app, FakeRequest(GET, "/documents")).get
      val responseBody = contentAsString(indexResult)

      responseBody must include("Quarkus, an awesome Kubernetes Native Java Framework")
      responseBody mustNot include("Quarkus, a Kubernetes Native Java Framework")
    }
  }

  "PUT /documents/:id with an invalid document" should {
    "return HTTP status BAD REQUEST (400)" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework"
          |}"""
        )).get
      status(result) mustBe BAD_REQUEST
    }

    "not update the database" in {
      val result = route(
        app,
        FakeRequest(
          PUT,
          "/documents/1",
          FakeHeaders(List("HOST"->"localhost", "Content-type"->"application/json")),
          """{
          |  "title": "Quarkus, an awesome Kubernetes Native Java Framework"
          |}"""
        )).get
      status(result) mustBe BAD_REQUEST

      val indexResult = route(app, FakeRequest(GET, "/documents")).get
      val responseBody = contentAsString(indexResult)

      responseBody mustNot include("Quarkus, an awesome Kubernetes Native Java Framework")
      responseBody must include("Quarkus, a Kubernetes Native Java Framework")
    }
  }

  "DELETE /documents/:id" should {
    "return HTTP status ACCEPTED (202)" in {
      val result = route(app, FakeRequest(DELETE, "/documents/1")).get
      status(result) mustBe ACCEPTED
    }

    "delete the given document from the database" in {
      val result = route(app, FakeRequest(DELETE, "/documents/1")).get
      status(result) mustBe ACCEPTED
      val indexResult = route(app, FakeRequest(GET, "/documents")).get
      val responseBody = contentAsString(indexResult)

      responseBody mustNot include("Quarkus")
    }
  }
}
