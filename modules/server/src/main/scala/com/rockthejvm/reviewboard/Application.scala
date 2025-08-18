package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.controllers.CompanyController
import com.rockthejvm.reviewboard.repositories.CompanyRepositoryLive
import com.rockthejvm.reviewboard.services.CompanyServiceLive
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server

import java.io.IOException

object Application extends ZIOAppDefault {

  private val startServer: ZIO[Any & Server & CompanyController, IOException, Unit] = for {
    controller <- ZIO.service[CompanyController]
    _          <- ZIO.logInfo("Starting server...")
    _          <- Server.install( // or Server.serve, but that blocks, so no need for ZIO.never below
                    ZioHttpInterpreter(
                      ZioHttpServerOptions.default
                    ).toHttp(controller.routes)
                  )
    _          <- Console.printLine("Server started on http://localhost:8080")
    _          <- ZIO.never
  } yield ()

  override def run: Task[Unit] =
    startServer
      .tapError(e => Console.printLine(s"Failed to start server: ${e.getMessage}"))
      .provide(
        // controllers
        CompanyController.layer,
        // services
        CompanyServiceLive.layer,
        // repositories
        CompanyRepositoryLive.layer,
        // provide the Quill context with
        Quill.DataSource.fromPrefix("rockthejvm.db"),
        Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase),
        // ZIO HTTP server
        Server.default
      )
}
