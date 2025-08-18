package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.Company
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.http.errors.HttpError
import com.rockthejvm.reviewboard.services.CompanyService
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import zio.*

class CompanyController private (service: CompanyService) extends CompanyEndpoints {

  // server = In => F[Out]
  // F can be Future, ZIO Task, Cats Effect IO, etc.
  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogic { _ =>
    service.getAll.either // this takes the error and converts it to a value
  //  ZIO[R, E, A] => ZIO[R, Nothing, Either[E, A]
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    service
      .getById(id)
      .someOrFail(
        new HttpError(
          StatusCode.NotFound,
          s"Company with id $id not found"
        ) // simulate fetching a company by ID
      )
      .tapError(e =>
        Console.printLine(s"Failed to fetch company by ID: $id, error: ${e.getMessage}")
      )
      .either
  }

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { request =>
    service.create(request).either
  }

  val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create
  ) // a list of all endpoints that this controller exposes
}

object CompanyController {
  val layer: URLayer[CompanyService, CompanyController] =
    ZLayer.fromFunction(new CompanyController(_))
}
