package com.rockthejvm.reviewboard.domain

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Company(
    id: Long,
    slug: String,
    name: String,
    url: String,
    location: Option[String] = None,
    country: Option[String] = None,
    industry: Option[String] = None,
    image: Option[String] = None,
    tags: List[String] = List(),
    premium: Boolean = false
) derives JsonCodec

object Company {
  val dummy =
    Company(1, "rock-the-jvm", "Rock the JVM", "https://rockthejvm.com")

  def makeSlug(name: String): String =
    name.toLowerCase.trim
      .replaceAll(" +", " ")
      .replaceAll(" ", "-")
}
