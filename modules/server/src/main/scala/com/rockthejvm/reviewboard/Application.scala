package com.rockthejvm.reviewboard

import zio.*
import com.rockthejvm.reviewboard.http.controllers.*
import com.rockthejvm.reviewboard.repositories.*
import com.rockthejvm.reviewboard.service.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.http.Server

object Application extends ZIOAppDefault {

  val startServer = for {
    companies <- ZIO.service[CompanyController]
    reviews <- ZIO.service[ReviewController]
    _ <- Console.printLine("Rock the JVM! Bootstrapping...")
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default.appendInterceptor(
          CORSInterceptor.default
        )
      ).toHttp(companies.routes ++ reviews.routes)
    )
  } yield ()

  override def run = startServer.provide(
    // controllers
    CompanyController.layer,
    ReviewController.layer,
    // service
    CompanyServiceLive.layer,
    ReviewServiceLive.layer,
    PaymentServiceLive.layer,
    // repo
    CompanyRepositoryLive.layer,
    ReviewRepositoryLive.layer,
    // infra layers
    Server.default,
    Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase), // SnakeCase.type
    Quill.DataSource.fromPrefix("rockthejvm.db")
  )
}
