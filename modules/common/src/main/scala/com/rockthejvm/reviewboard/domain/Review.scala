package com.rockthejvm.reviewboard.domain

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec
import java.time.Instant

final case class Review(
    id: Long,        // PK
    companyId: Long,
    userId: Long,    // FK
    management: Int, // 1-5
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
) derives JsonCodec

object Review {
  def empty(companyId: Long) = Review(
    -1L,
    companyId,
    -1L,
    5,
    5,
    5,
    5,
    5,
    "",
    Instant.now(),
    Instant.now()
  )
}
