package com.rockthejvm.reviewboard.http.controllers

import zio.*
import sttp.tapir.server.ServerEndpoint
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.service.{CompanyService, PaymentService}

class CompanyController private (paymentService: PaymentService, service: CompanyService) extends CompanyEndpoints {

  // server = In => F[Out]
  // F can be Future, ZIO Task, Cats Effect IO, ....

  val getAll: ServerEndpoint[Any, Task] = // invocation of the endpoint returns a Task = ZIO[Any, Throwable, A]
    getAllEndpoint.serverLogic { _ => // Task[Either[Throwable, List[Company]]]
      service.getAll()
        .either // this takes the error and puts it in the value
        // ZIO[R, E, A] => ZIO[R, Nothing, Either[E,A]]
    }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint.serverLogic { id =>
      service.getById(id.toLong)
        .either
    }

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic { payload =>
      service.create(payload)
        .either
    }

  val createPremium: ServerEndpoint[Any, Task] =
    createPremiumEndpoint.serverLogic { payload =>
      service.create(payload)
        .flatMap(company => paymentService.createCheckoutSession(company.id))
        .map {
          case None => ""
          case Some(session) => session.getUrl
        }
        .either

      // start a Stripe checkout session => return the URL of the checkout page!!
    }

  val webhook: ServerEndpoint[Any, Task] =
    webhookEndpoint.serverLogic { (payload, signature) =>
      paymentService.handleWebhookEvent(payload, signature, id => service.upgrade(id.toLong))
        .unit
        .either
    }

  val routes = List(getAll, getById, create, createPremium, webhook)
}

object CompanyController {
  val layer = ZLayer.fromFunction(new CompanyController(_,_))
}
