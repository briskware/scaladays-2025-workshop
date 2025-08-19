package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

import zio.json.JsonCodec
import zio.json.DeriveJsonCodec
import java.time.Instant

final case class CreateReviewRequest(
    companyId: Long,
    userId: Long,    // FK
    management: Int, // 1-5
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
) derives JsonCodec
