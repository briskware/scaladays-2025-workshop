package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zio.*

import com.rockthejvm.reviewboard.components.*
import com.rockthejvm.reviewboard.domain.*

import sttp.model.Uri
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.client.sttp.SttpClientInterpreter

object CompaniesPage {

  // TODO - create an EventBus which can store a list of companies
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
  // TODO - replace the dummy company with a reactive element, which reads from the event bus
  // TODO - refactor the backend calls as necessary

  def apply() =
    sectionTag(
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
            CompanyComponents.renderCompany(Company.dummy)
          )
        )
      )
    )
}
