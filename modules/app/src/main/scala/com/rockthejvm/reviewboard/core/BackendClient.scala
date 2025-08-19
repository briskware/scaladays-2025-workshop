package com.rockthejvm.reviewboard.core

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import sttp.client3.impl.zio.FetchZioBackend
import sttp.model.Uri
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*

trait BackendClient {
  val companies: CompanyEndpoints

  def call[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(payload: I): Task[O]
}

class BackendClientLive(rootUri: String) extends BackendClient {
  private val backend     = FetchZioBackend()       // tapir type
  private val interpreter = SttpClientInterpreter() // tapir type

  override val companies: CompanyEndpoints = new CompanyEndpoints()

  override def call[I, E <: Throwable, O](
      endpoint: Endpoint[Unit, I, E, O, Any]
  )(payload: I): Task[O] = {
    val endpointFun = interpreter.toRequestThrowDecodeFailures(
      endpoint,
      Uri.parse(rootUri).toOption // the URL of the server (proxied via vite)
    )

    // the request is empty (unit), as per the endpoint definition
    val request = endpointFun(payload)

    backend
      .send(request) // this returns a ZIO[Response, Throwable]
      .map(_.body)
      .absolve       // Task[List[Company]] // this converts the Either to a Task[List[Company]]
  }
}

object BackendClientLive {
  val layer: ZLayer[Any, Throwable, BackendClient] = ZLayer.succeed(new BackendClientLive("/"))
}
