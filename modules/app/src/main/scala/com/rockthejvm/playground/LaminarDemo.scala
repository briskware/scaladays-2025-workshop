package com.rockthejvm.playground

import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import org.scalajs.dom
import scala.util.Try

object LaminarDemo {
  val staticContent =
    div(
      // modifiers
      styleAttr := "color:red", // <div style="color:red">
      p("This is an app"),
      p("rock the JVM but also JS")
    )

  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
    render(
      containerNode,
      staticContent
    )
  }
}
