package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.Review
import com.rockthejvm.reviewboard.http.errors.HttpError
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
//import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.*
import sttp.tapir.EndpointInput.AuthType.Http
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

trait ReviewEndpoints {
  // GET /reviews/id -> Option[Review]
  val getByIdEndpoint: Endpoint[Unit, Long, Throwable, Review, Any] =
    endpoint.get
      .prependIn("reviews" / path[Long]("id")) // /reviews/{id}
      .out(jsonBody[Review])
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .name("getById")
      .tag("reviews")
      .description("Get a review by ID")

  // GET /reviews/company/id -> List[Review]
  val getByCompanyIdEndpoint: Endpoint[Unit, Long, Throwable, List[Review], Any] =
    endpoint.get
      .prependIn("reviews" / "company" / path[Long]("id")) // /reviews/company/{id}
      .out(jsonBody[List[Review]])
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .name("getByCompanyId")
      .tag("reviews")
      .description("Get all reviews for a company by ID")

  // POST /reviews -> {} -> Review
  val createEndpoint: Endpoint[Unit, CreateReviewRequest, Throwable, Review, Any] =
    endpoint.post
      .prependIn("reviews")              // /reviews
      .in(jsonBody[CreateReviewRequest]) // the request body maps to a CreateReviewRequest
      .out(jsonBody[Review])             // the response body is a JSON body that maps to a Review
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .name("createReview")
      .tag("reviews")
      .description("Create a new review")
}
