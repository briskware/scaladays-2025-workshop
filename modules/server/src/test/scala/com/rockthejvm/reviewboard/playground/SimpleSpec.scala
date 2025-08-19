package com.rockthejvm.reviewboard.playground

import com.rockthejvm.reviewboard.syntax.*
import zio.*
import zio.test.*
import zio.test.Assertion.equalTo

object SimpleSpec extends ZIOSpecDefault {
  override def spec =
    suite("SimpleSpec")(
      test("simple assertion") {
        assert(1 + 2)(equalTo(3))
        && assert(1 + 1)(Assertion.assertion("two")(_ == 2))
      },
      test("simple ZIO assertion") {
        val aZIO = ZIO.succeed(42)
        aZIO.must(_ == 42)
      }
    )
}
