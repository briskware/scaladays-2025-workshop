package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.Company
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import sttp.tapir.server.ServerEndpoint
import zio.*

class CompanyController extends CompanyEndpoints {

  // server = In => F[Out]
  // F can be Future, ZIO Task, Cats Effect IO, etc.
  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogic { _ =>
    ZIO
      .attempt(List(Company.dummy))
      .either // this takes the error and converts it to a value
  //  ZIO[R, E, A] => ZIO[R, Nothing, Either[E, A]
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(Company.dummy.copy(id = id)) // simulate fetching a company by ID
      .either
  }

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { request =>
    ZIO
      .attempt(
        Company.dummy.copy(id = 42, name = request.name, url = request.url)
      ) // simulate creating a company
      .either
  }

  val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create
  ) // a list of all endpoints that this controller exposes
}

object CompanyController {
  val live: URLayer[Any, CompanyController] = ZLayer.succeed(new CompanyController)
}
