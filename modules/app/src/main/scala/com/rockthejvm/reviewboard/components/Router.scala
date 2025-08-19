package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*

import com.rockthejvm.reviewboard.pages.*

object Router {
  val externalUrlBus = EventBus[String]() // event bus of URLs that the browser pushed to ME

  def apply() =
    mainTag(
      onMountCallback(ctx =>
        externalUrlBus.events
          .foreach(url => dom.window.location.href = url)(using ctx.owner)
      ),
      routes(
        div(
          cls := "container-fluid",
          (pathEnd | path("companies")) { // at / or /companies
            CompaniesPage()
          },
          path("company" / long) { id =>
            CompanyPage(id)
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )
}
