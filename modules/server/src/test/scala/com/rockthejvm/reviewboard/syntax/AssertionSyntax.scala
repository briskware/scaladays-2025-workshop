package com.rockthejvm.reviewboard.syntax

import zio.*
import zio.test.*

extension [R,E,A](zio: ZIO[R,E,A])
  def must(predicate: (=> A) => Boolean): ZIO[R, E, TestResult] =
    assertZIO(zio)(Assertion.assertion("test assertion")(predicate))
    
/*
    Scala 2 equivalent:
    
implicit class ZIOTestExtension[R,E,A](zio: ZIO[R,E,A]) {
  def must(predicate: (=> A) => Boolean): ZIO[R, E, TestResult] =
    assertZIO(zio)(Assertion.assertion("test assertion")(predicate))
}
*/
    