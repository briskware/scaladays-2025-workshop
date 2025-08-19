package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zio.*
import com.rockthejvm.reviewboard.components.*
import com.rockthejvm.reviewboard.domain.*
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import sttp.model.Uri
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.client.sttp.SttpClientInterpreter

object CompaniesPage {

  // TODO - create an EventBus which can store a list of companies
  val companiesBus = EventBus[List[Company]]()

  /*
     TODO - use STTP client to trigger a backend call, then use the value obtained to emit into the event bus
      - create a FetchZioBackend and a SttpClientInterpreter
      - instantiate your CompanyEndpoints
      - define your endpoint by invoking your interpreter on the endpoint definition and the URL of the server
      - define your request by invoking the endpoint on the payload (empty)
      - call the send method on the backend, with the request - this returns a ZIO
      - execute the ZIO
      - tap the ZIO such that when it has a value, you emit it into the event bus
   */
  def getCompaniesNaive(): Fiber.Runtime[Throwable, List[Company]] = {
    val backend      = FetchZioBackend()       // tapir type
    val interpreter  = SttpClientInterpreter() // tapir type
    val endpoints    = new CompanyEndpoints()  // can instantiate the endpoints from the common module
    val endpoint     = interpreter.toRequestThrowDecodeFailures(
      endpoints.getAllEndpoint, // the endpoint definition,
      Uri.parse("/").toOption   // the URL of the server
    )
    val request      = endpoint(())            // the request is empty (unit), as per the endpoint definition
    val companiesZIO = backend
      .send(request) // this returns a ZIO[Response, Throwable]
      .map(_.body)
      .absolve       // Task[List[Company]] // this converts the Either to a Task[List[Company]]
    Unsafe.unsafely {
      Runtime.default.unsafe.fork(
        companiesZIO.tap { companies =>
          ZIO.attempt(
            companiesBus.emit(companies)
          ) // emit the list of companies into the event bus
        }
      )
    }
  }

  // TODO - replace the dummy company with a reactive element, which reads from the event bus
  // TODO - refactor the backend calls as necessary

  def apply() =
    sectionTag(
      onMountCallback(_ => getCompaniesNaive()),
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "Rock the JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-12",
            children <-- companiesBus.events.map(_.map(CompanyComponents.renderCompany))
          )
        )
      )
    )
}
