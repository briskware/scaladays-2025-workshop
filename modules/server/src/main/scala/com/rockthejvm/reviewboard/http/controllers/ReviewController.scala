package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.service.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends ReviewEndpoints {

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      service.getById(id)
        .either
    }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint.serverLogic { id =>
      service.getByCompanyId(id.toLong)
        .either
    }

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic { payload =>
      service.create(payload)
        .either
    }

  val routes = List(create, getByCompanyId, getById)
}

object ReviewController {
  val layer = ZLayer.fromFunction(new ReviewController(_))
}
