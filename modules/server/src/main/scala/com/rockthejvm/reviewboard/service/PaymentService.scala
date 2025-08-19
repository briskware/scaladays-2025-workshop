package com.rockthejvm.reviewboard.service

import com.stripe.Stripe
import zio.*
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import scala.jdk.OptionConverters.*

trait PaymentService {
  def createCheckoutSession(id: Long): Task[Option[Session]]
  def handleWebhookEvent[A](payload:String, signature:String, action: String => Task[A]): Task[Option[A]]
}

class PaymentServiceLive private extends PaymentService {
  override def createCheckoutSession(id: Long) =
    ZIO.attempt(
        SessionCreateParams.builder
          .setSuccessUrl("https://localhost:5173")
          .addLineItem(
            SessionCreateParams.LineItem.builder
              .setPrice("TODO")
              .setQuantity(2L)
              .build
          )
          .setClientReferenceId(id.toString)
          .setMode(SessionCreateParams.Mode.PAYMENT)
          .build
      )
      .map(params => Session.create(params))
      .map(Option(_))

  override def handleWebhookEvent[A](payload: String, signature: String, action: String => Task[A]) =
    ZIO
      .attempt(
        Webhook.constructEvent(payload, signature, "TODO")
      )
      .flatMap { event =>
        event.getType match {
          case "checkout.session.completed" =>
            ZIO.foreach(
              event.getDataObjectDeserializer
                .getObject.toScala
                .map(_.asInstanceOf[Session])
                .map(_.getClientReferenceId)
            )(action)
        }
      }
}

object PaymentServiceLive {
  val layer = ZLayer {
    for {
      _ <- ZIO.attempt(Stripe.apiKey = "TODO")
      service <- ZIO.succeed(new PaymentServiceLive)
    } yield service
  }
}
