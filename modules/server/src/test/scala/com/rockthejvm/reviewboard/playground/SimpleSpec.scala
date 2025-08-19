package com.rockthejvm.reviewboard.playground

import zio.*
import zio.test.*

import com.rockthejvm.reviewboard.syntax.*

object SimpleSpec extends ZIOSpecDefault {
  override def spec =
    // add a test suite
    suite("SimpleSpec")(
      // add a test
      test("simple assertion")(
        assert(1 + 2)(Assertion.assertion("three")(_ == 3))
      ),
      // chain multiple tests with ','
      test("simple ZIO assertion") {
        // define your programs
        val aZIO = ZIO.succeed(42)
        aZIO.must(_ == 42)
      }
    ).provide(
      // add layers required by any test suite here
    )
}
