package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

case class CreateReviewRequest(
                                companyId: Long,
                                userId: Long,    // FK
                                management: Int, // 1-5
                                culture: Int,
                                salary: Int,
                                benefits: Int,
                                wouldRecommend: Int,
                                review: String,
) derives JsonCodec
