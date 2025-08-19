package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.Review
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.errors.HttpError
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends ReviewEndpoints {
  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { request =>
    service.createReview(request).either
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    service
      .getReviewById(id)
      .someOrFail(
        new HttpError(
          StatusCode.NotFound,
          s"Review with id $id not found"
        )
      )
      .tapError(e =>
        Console.printLine(s"Failed to fetch review by ID: $id, error: ${e.getMessage}")
      )
      .either
  }

  val getByCompanyId: ServerEndpoint[Any, Task] = getByCompanyIdEndpoint.serverLogic { companyId =>
    service.getReviewsByCompanyId(companyId).either
  }

  val routes: List[ServerEndpoint[Any, Task]] = List(
    create,
    getById,
    getByCompanyId
  )

}

object ReviewController {
  val layer: URLayer[ReviewService, ReviewController] =
    ZLayer.fromFunction(new ReviewController(_))
}
