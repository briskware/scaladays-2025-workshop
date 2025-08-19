package com.rockthejvm.reviewboard.core

import com.raquo.airstream.eventbus.EventBus
import sttp.tapir.Endpoint
import zio.*

object ZJS {

  def backendCall = ZIO.serviceWithZIO[BackendClient]

  extension [E, A](zio: ZIO[BackendClient, E, A])
    def emitTo(eventBus: EventBus[A]) =
      Unsafe.unsafely {
        Runtime.default.unsafe.fork {
          zio
            .tap(value => ZIO.attempt(eventBus.emit(value)))
            .provide(BackendClientLive.layer)
        }
      }

  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    def apply(payload: I): ZIO[BackendClient, Throwable, O] = {
      ZIO.serviceWithZIO[BackendClient](_.call(endpoint)(payload))
    }
}
