package com.rockthejvm.reviewboard.playground

import zio.*

import java.util.UUID

object NewsletterApp extends ZIOAppDefault {

  /** Dependency injection
    */
  case class User(name: String, email: String)
  // layer 4 - application
  class Newsletter(emailService: EmailService, userDatabase: UserDatabase) {
    def emailSubscribers(): Task[Unit] = for {
      emailList <- userDatabase.getList
      _         <- ZIO.foreach(emailList)(emailService.sendEmail(_, "Newsletter", "Hello!"))
    } yield ()
  }
  object Newsletter {
    def smartConstructor: ZLayer[EmailService & UserDatabase, Nothing, Newsletter] = {
      ZLayer.fromFunction(new Newsletter(_, _))
    }
  }

  // layer 3 - services
  class EmailService {
    def sendEmail(user: User, subject: String, body: String): Task[Unit] = {
      // imagine this sends an email
      ZIO.succeed(println(s"Sending email to ${user.email} with subject '$subject'"))
    }
  }
  object EmailService {
    def smartConstructor
        : ULayer[EmailService] = { // ULayer is a ZLayer with no dependencies and is unfailable
      ZLayer.succeed(new EmailService())
    }
  }

  // layer 2 - database
  class UserDatabase(connectionPool: ConnectionPool) {
    def getList: Task[List[User]] = for {
      // imagine this fetches users from a database
      _ <- connectionPool.get
      list <- ZIO.succeed(
        List(
          User("Stefan", "stefan@user.com"),
          User("John", "john@user.com")
        )
      )
    } yield list
  }
  object UserDatabase {
    def smartConstructor_v1(connectionPool: ConnectionPool): ZLayer[Any, Nothing, UserDatabase] = {
      ZLayer.succeed(new UserDatabase(connectionPool))
    }
    def smartConstructor: ZLayer[ConnectionPool, Nothing, UserDatabase] = {
      ZLayer.fromFunction(connPool => UserDatabase(connPool))
    }
  }

  // layer 1 - connections
  class ConnectionPool(nConn: Int) {
    def get: Task[Connection] = ZIO.succeed(Connection(UUID.randomUUID().toString))
  }

  object ConnectionPool {
    def smartConstructor_1(nConn: Int): ZLayer[Any, Nothing, ConnectionPool] = {
      ZLayer.succeed(new ConnectionPool(nConn))
    }
  }

  case class Connection(id: String)

  // describe the program in ZIO terms
  val program: ZIO[Newsletter, Throwable, Unit] = for {
    newsletter <- ZIO
      .service[Newsletter] // assume that a newsletter exists, if so, return it and use it
    _ <- newsletter.emailSubscribers() // run the program
  } yield ()

  // run the program by providing the dependencies
  override def run: Task[Unit] = program.provide(
    // we only create the right instances of the layers we need here
    Newsletter.smartConstructor,
    EmailService.smartConstructor,
    UserDatabase.smartConstructor,
    ConnectionPool.smartConstructor_1(10) // 10 connections in the pool
  )
}
