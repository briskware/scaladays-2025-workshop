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

  override def run = aLast

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
