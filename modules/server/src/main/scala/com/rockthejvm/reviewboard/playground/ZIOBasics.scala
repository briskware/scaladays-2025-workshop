package com.rockthejvm.reviewboard.playground

import zio.*

import java.util.UUID
import scala.io.StdIn

object ZIOBasics extends ZIOAppDefault {

  // ZIO[-R, +E, +A] ~~~ R => Either[E, A]

  class Pet
  class Dog extends Pet

  // Dog <: Pet => List[Dog] <: List[Pet] => List[+A]
  // Dog <: Pet => Vet[Pet] <: Vet[Dog] => Vet[-A]

  // if your type PRODUCES things => COVARIANT
  // if ... CONSUMES things => CONTRAVARIANT
  // else, INVARIANT

  // success
  val meaningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  // failure
  val aFailure: ZIO[Any, String, Nothing] = ZIO.fail("this is a problem")
  // suspension
  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningOfLife) // "lazy" evaluation

  // map/flatMap/for...

  val improvedMOL = meaningOfLife.map(_ * 10)
  val printingMOL = meaningOfLife.flatMap(mol => ZIO.succeed(println(mol)))

  val smallProgram = for {
    name <- Console.printLine("what's your name?") // ZIO[..., String]
    _ <- Console.print(s"hello, $name") // ZIO[..., Unit]
  } yield () // ZIO[..., Unit]

  // error handling
  val anAttempt: ZIO[Any, Throwable, Int] = ZIO.attempt {
    println("Trying something")
    throw RuntimeException("Boom!")
    //    ^^^^^^^^^^^^^^^^^^^^^^^^^ ERROR = mentioned in the type sig
  }

  val bigBoom: ZIO[Any, Nothing, Int] = ZIO.succeed(throw new RuntimeException("boom"))
  //                                                          ^^^^^^^^^^^^^^^^^^^^^^^^ DEFECT = not mentioned in the type signature

  val catchError = anAttempt.catchAll(e => Console.printLine(s"I've just survived. Caught $e"))
  // catchSome, catchAllDefect ...


  /**
   * Exercises:
   */
  // 1 - sequence two ZIOs and take the LAST value
  def last[E, A, B](zioa: ZIO[Any,E,A], ziob: ZIO[Any,E,B]): ZIO[Any,E,B] =
    for {
      _ <- zioa
      b <- ziob
    } yield b

  def last_v2[E, A, B](zioa: ZIO[Any,E,A], ziob: ZIO[Any,E,B]): ZIO[Any,E,B] =
    zioa *> ziob

  // 2 - run a zio forever
  def infinite[E,A](zio: ZIO[Any, E, A]): ZIO[Any, E, A] =
    for {
      _ <- zio
      r <- infinite(zio)
    } yield r

  val aLoop = infinite(Console.printLine("Trying again") *> ZIO.sleep(1.second))

  // DI
  case class User(name: String, email: String)

  // layer 4 - "application"
  class Newsletter private (emailService: EmailService, userDatabase: UserDatabase) {
    def emailSubscribers() = for {
      emailList <- userDatabase.getList
      _ <- ZIO.foreach(emailList)(emailService.sendEmail)
      //               ^^^^^^^^^  ^^^^^^^^^^^^^^^^^^^^^
      //             collection    effect that runs on every element
    } yield ()
  }

  object Newsletter {
    def smartConstructor: ZLayer[EmailService & UserDatabase, Nothing, Newsletter] =
      ZLayer.fromFunction(new Newsletter(_, _))
  }

  // layer 3 - services
  class EmailService private {
    def sendEmail(user: User): Task[Unit] =
      Console.printLine(s"Emailing $user")
  }

  object EmailService {
    def smartConstructor: ULayer[EmailService] =
      ZLayer.succeed(new EmailService)
  }

  // layer 2 - database
  class UserDatabase private (connectionPool: ConnectionPool) {
    def getList: Task[List[User]] = for {
      _ <- connectionPool.get
      list <- ZIO.succeed(List(
        User("Daniel", "daniel@rockthejvm.com"),
        User("Alice", "alice@example.com"),
      ))
    } yield list
  }

  object UserDatabase {
    def smartConstructor_v1(connectionPool: ConnectionPool): ZLayer[Any, Nothing, UserDatabase] =
      ZLayer.succeed(new UserDatabase(connectionPool))

    def smartConstructor: ZLayer[ConnectionPool, Nothing, UserDatabase] =
      ZLayer.fromFunction(connPool => UserDatabase(connPool))
  }

  // layer 1 - connections
  class ConnectionPool private (nConn: Int) {
    def get: Task[Connection] = // ZIO[Any, Throwable, A]
      ZIO.succeed(Connection(UUID.randomUUID().toString))
  }

  object ConnectionPool {
    // ZIO Layers aka ZLayers
    // ZLayer[-R, +E, +A], like ZIO effects
    def smartConstructor(nConn: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(new ConnectionPool(nConn))
  }

  case class Connection(id: String)

  /*
    val connPool = ...
    val db = ...
    val email = ...
    val newsletter = Newsletter.apply(...)
    newsletter.emailSubscribers() ....
    // build deps manually
    // pass deps manually
   */

  // 1. describe the program
  val program = for {
    newsletter <- ZIO.service[Newsletter] // assume that a Newsletter exists, if so, return it and use it
    _ <- newsletter.emailSubscribers()
  } yield ()

  // 2. provide all the layers in whatever order, the ZIO runtime will take care to pass the right deps
  override def run = program.provide(
    // this takes care to build the right instances AND pass the right dependencies
    Newsletter.smartConstructor,
    EmailService.smartConstructor,
    UserDatabase.smartConstructor,
    ConnectionPool.smartConstructor(10)
  )
}
