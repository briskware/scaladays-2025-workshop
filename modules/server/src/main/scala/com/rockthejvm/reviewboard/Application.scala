package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.controllers.{CompanyController, ReviewController}
import com.rockthejvm.reviewboard.repositories.{CompanyRepositoryLive, ReviewRepositoryLive}
import com.rockthejvm.reviewboard.services.{CompanyServiceLive, ReviewServiceLive}
import com.stripe.service.ReviewService
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server

import java.io.IOException

object Application extends ZIOAppDefault {

  private val startServer
      : ZIO[Any & Server & CompanyController & ReviewController, IOException, Unit] = for {
    companyController <- ZIO.service[CompanyController]
    reviewController  <- ZIO.service[ReviewController]
    _                 <- ZIO.logInfo("Starting server...")
    _                 <- Server.install( // or Server.serve, but that blocks, so no need for ZIO.never below
                           ZioHttpInterpreter(
                             ZioHttpServerOptions.default
                               // below is not needed, if you proxy via the vite.config.js
//                               .appendInterceptor(
//                                 CORSInterceptor.default // this allows for CORS from different origins
//                               )
                           ).toHttp(companyController.routes ++ reviewController.routes)
                         )
    _                 <- Console.printLine("Server started on http://localhost:8080")
    _                 <- ZIO.never
  } yield ()

  override def run: Task[Unit] =
    startServer
      .tapError(e => Console.printLine(s"Failed to start server: ${e.getMessage}"))
      .provide(
        // controllers
        CompanyController.layer,
        ReviewController.layer,
        // services
        CompanyServiceLive.layer,
        ReviewServiceLive.layer,
        // repositories
        CompanyRepositoryLive.layer,
        ReviewRepositoryLive.layer,
        // provide the Quill context with
        Quill.DataSource.fromPrefix("rockthejvm.db"),
        Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase),
        // ZIO HTTP server
        Server.default
      )
}
