package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

case class CreateCompanyRequest (
    name: String,
    url: String
) derives JsonCodec
