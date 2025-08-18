package com.rockthejvm.reviewboard.playground

import zio.*

import java.io.IOException
import java.util.UUID
import scala.annotation.tailrec
import scala.io.StdIn

object ZIOBasics extends ZIOAppDefault {

  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)

  val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("An error occurred")

  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife) // lazy evaluation

  val improvedMOL = meaningOfLife.map(_ * 10)
  val printingMOL =
    meaningOfLife.flatMap(mol => ZIO.succeed(println(s"The meaning of life is $mol")))

  val smallProgram: ZIO[Any, IOException, Unit] = for {
    _    <- Console.printLine("what's your name?")
    name <- Console.readLine("")
    _    <- Console.printLine(s"Hello, $name!")
  } yield ()

  // error handling
  val anAttempt: ZIO[Any, Throwable, Int] = ZIO.attempt {
    throw RuntimeException("Boom!")
  }

  val bigBoom: ZIO[Any, Nothing, Int] =
    ZIO.succeed(throw new RuntimeException("Boom!")) // this is a defect

  val catchError: ZIO[Any, IOException, Any] =
    anAttempt.catchAll(e => Console.printLine("I caught an error: " + e.getMessage))

  /** Excercise 1 - sequence two ZIOs and take the LAST value
    */
  def last[E, A, B](zioa: ZIO[Any, E, A], ziob: ZIO[Any, E, B]): ZIO[Any, E, B] = for {
    _ <- zioa
    b <- ziob
  } yield b // or just "zioa *> ziob"

  val aLast = last(catchError, smallProgram)

  /** Exercise 2 - run zio forever
    */
  // @tailrec // why does this not compile?
  def infinite[E, A](zio: ZIO[Any, E, A]): ZIO[Any, E, A] = {
    zio *> infinite(zio)
  }

  def aLoop = infinite(Console.printLine("Trying again") *> ZIO.sleep(1.second))

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

// ZIO[-R, +E, +A] (zio is covariant in E and A, contravariant in R)

// Similar to Either
// ZIO[-R, +E, +A] ~~~ Either[E, A]

// Dog <: Animal => List[Dog] <: List[Animal] => List[+A] (covariant type parameter A)
// Pet <: Animal => Vet[Pet] <: Vet[Dog] => Vet[-A] (contravariant type parameter A)

// if your type produces things => COVARIANT
// if your type consumes things => CONTRAVARIANT
// else INVARIANT

class Animal
class Dog extends Animal
