package com.rockthejvm.reviewboard.http.controllers

import zio.*
import sttp.tapir.server.ServerEndpoint

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.domain.Company

class CompanyController private extends CompanyEndpoints {

  // server = In => F[Out]
  // F can be Future, ZIO Task, Cats Effect IO, ....

  val getAll: ServerEndpoint[Any, Task] = // invocation of the endpoint returns a Task = ZIO[Any, Throwable, A]
    getAllEndpoint.serverLogic { _ => // Task[Either[Throwable, List[Company]]]
      ZIO.attempt(
        List(
          Company.dummy
        )
      )
        .either // this takes the error and puts it in the value
        // ZIO[R, E, A] => ZIO[R, Nothing, Either[E,A]]
    }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { i =>
      ZIO.attempt(Company.dummy.copy(id = i.toLong))
        .either
    }

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic { payload =>
      ZIO.attempt(Company.dummy.copy(name = payload.name, url = payload.url, slug = Company.makeSlug(payload.name)))
        .either
    }

  val routes = List(getAll, getById, create)
}

object CompanyController {
  val live = ZLayer.succeed(new CompanyController)
}
