package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import com.rockthejvm.reviewboard.domain.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest

trait ReviewEndpoints extends BaseEndpoint {
  // GET /reviews/id -> Option[Review]
  val getByIdEndpoint =
    baseEndpoint
      .get
      .in("reviews" / path[Long]("id"))
      .out(jsonBody[Option[Review]])

  // GET /reviews/company/companyId -> List[Review] for a particular company id
  val getByCompanyIdEndpoint =
    baseEndpoint
      .get
      .in("reviews" / "company" / path[Long]("companyId"))
      .out(jsonBody[List[Review]])

  // POST /reviews {} -> Review
  val createEndpoint =
    baseEndpoint
      .post
      .in("reviews")
      .in(jsonBody[CreateReviewRequest])
      .out(jsonBody[Review])
}
