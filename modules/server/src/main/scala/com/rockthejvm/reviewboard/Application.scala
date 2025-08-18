package com.rockthejvm.reviewboard

import zio.*
import com.rockthejvm.reviewboard.http.controllers.*
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.http.Server

object Application extends ZIOAppDefault {

  val startServer = for {
    controller <- ZIO.service[CompanyController]
    _ <- Console.printLine("Rock the JVM! Bootstrapping...")
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(controller.routes)
    )
  } yield ()

  override def run = startServer.provide(
    // controllers
    CompanyController.live,
    // infra layers
    Server.default
  )
}
