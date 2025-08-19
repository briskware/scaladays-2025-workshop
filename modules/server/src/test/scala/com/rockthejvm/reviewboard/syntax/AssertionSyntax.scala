package com.rockthejvm.reviewboard.syntax

import zio.*
import zio.test.*

extension [R, E, A](zio: ZIO[R, E, A])
  def must(
      predicate: (=> A) => Boolean,
      message: String = "Assertion failed"
  ): ZIO[R, E, TestResult] =
    assertZIO(zio)(Assertion.assertion(message)(predicate))
