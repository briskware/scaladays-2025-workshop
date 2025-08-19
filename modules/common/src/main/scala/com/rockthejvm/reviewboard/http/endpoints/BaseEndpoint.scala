package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import com.rockthejvm.reviewboard.http.errors.HttpError

trait BaseEndpoint {
  val baseEndpoint =
    endpoint
      .prependIn("api") // /api before anything
      .errorOut(statusCode and plainBody[String]) // if the http server throws an error, this endpoint will surface the status code + a string
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      //                                        ^^^^^^^^^^^^^^^^ this does the reverse: in case of HTTP errors, get the (statuscode and string) => Throwable
      //                      ^^^^^^^^^^^^^^^^ if the server fails => map the Throwable into whatever the endpoint specified (this case is a tuple (status code, string))
}
