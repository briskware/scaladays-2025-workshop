package com.rockthejvm.reviewboard.http.errors

import sttp.model.StatusCode

case class HttpError(statusCode: StatusCode, message: String, cause: Throwable)
    extends RuntimeException(message, cause)

object HttpError {
  def decode(tuple: (StatusCode, String)): Throwable = {
    HttpError(tuple._1, tuple._2, new RuntimeException(tuple._2))
  }
  def encode(error: Throwable): (StatusCode, String) = error match {
    case he: HttpError => (he.statusCode, he.message)
    case _             => (StatusCode.InternalServerError, error.getMessage)
  }
}
