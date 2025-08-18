package com.rockthejvm.reviewboard.service

import zio.*
import java.time.Instant

import com.rockthejvm.reviewboard.domain.*
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository

trait ReviewService {
  def create(req: CreateReviewRequest): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService {
  def create(req: CreateReviewRequest): Task[Review] =
    repo.create(
      Review(
        -1,
        companyId = req.companyId,
        userId = req.userId,
        management = req.management,
        culture = req.culture,
        salary = req.salary,
        benefits = req.benefits,
        wouldRecommend = req.wouldRecommend,
        review = req.review,
        Instant.now(),
        Instant.now(),
      )
    )

  def getByCompanyId(id: Long): Task[List[Review]] =
    repo.getByCompanyId(id)

  def getById(id: Long): Task[Option[Review]] =
    repo.getById(id)
}

object ReviewServiceLive {
  val layer = ZLayer.fromFunction(new ReviewServiceLive(_))
}