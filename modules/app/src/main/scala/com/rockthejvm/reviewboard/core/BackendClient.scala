package com.rockthejvm.reviewboard.core

import zio.*
import sttp.tapir.Endpoint
import sttp.model.Uri
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.client.sttp.SttpClientInterpreter

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints

trait BackendClient {
  val companies: CompanyEndpoints

  def call[I, E <: Throwable,O](endpoint: Endpoint[Unit, I, E, O, Any])(payload: I): Task[O]
}

class BackendClientLive(rootUri: String) extends BackendClient {
  // infra
  val backend = FetchZioBackend() // tapir type
  val interpreter = SttpClientInterpreter() // tapir type

  // endpoint groups
  override val companies = new CompanyEndpoints

  override def call[I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])(payload: I): Task[O] = {
    val endpointFun = interpreter.toRequestThrowDecodeFailures(
      endpoint, // endpoint definition
      Uri.parse(rootUri).toOption // server URL
    )

    val request = endpointFun(payload) // invoke the endpoint on the payload (Unit in this case)

    backend.send(request).map(_.body).absolve // Task[List[Company]]
  }
}

object BackendClientLive {
  val layer: ZLayer[Any, Nothing, BackendClient] =
    ZLayer.succeed(new BackendClientLive("/"))
}
