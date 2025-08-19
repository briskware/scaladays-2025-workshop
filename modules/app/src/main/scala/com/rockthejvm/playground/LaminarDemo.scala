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
      p("Lock and load")
    )

  // reactive variables = "observable"

  // 1. EventStream => produce elements, infinite List[A]
  val ticks = EventStream.periodic(1000) // produces a new int every 1000ms
  val countingSeconds = ticks.map(number => span(s"$number seconds")) // EventStream

  val timeUpdated =
    div(
      span("Time since loaded: "),
      child <-- countingSeconds,
    )

  // 2. EventBus => like an EventStream, but you can "push" elements to it
  val clicksBus = EventBus[Int]()
  val clickEvents = clicksBus.events // EventStream that will "read" from the EventBus
    .scanLeft(0)(_ + _) // [1,1,1,1,1,1,1,1,1,1,1,1,1,1] => [1,2,3,4,5,6,7,8,9,10,11,12,13, ... ]

  val countingClicks =
    div(
      span("Clicks counted: "),
      child <-- clickEvents.map(number => span(s"$number clicks")),
      br(),
      button(
        onClick.mapTo(1) --> clicksBus, // pushes events to this bus
        "Click me"
      )
    )

  // 3 - Signal - same as EventStream, but it also contains "state" = "latest value"
  val countSignal = clicksBus.events.scanLeft(0)(_ + _).observe(using new OneTimeOwner(() => ())) // Signal[Int]
  val queryBus = EventBus[Unit]()

  val checkingClicks =
    div(
      span("Clicks registered since loaded: "),
      child <-- queryBus.events.map(_ => countSignal.now()),
      br(),
      button(
        onClick.mapTo(1) --> clicksBus,
        "Click me"
      ),
      br(),
      button(
        onClick.mapTo(()) --> queryBus,
        "Check number of clicks"
      ),
    )

  // 4 - Var = EventBus (write) + Signal (read + state)
  val countVar = Var[Int](0)
  val clicksWithVar =
    div(
      span("Clicks registered since loaded: "),
      child <-- countVar.signal.map(_.toString),
      br(),
      button(
        // procedural style
        // onClick --> (_ /* mouse event */ => countVar.set(countVar.now() + 1)),

        // foldLeft-style
        onClick --> countVar.updater((currentValue, event) => currentValue + 1),

        // functor-style
        // onClick --> countVar.writer.contramap(event => countVar.now() + 1),

        "Click me"
      ),
      br(),
      button(
        onClick --> (_ => countVar.set(0)),
        "Reset clicks"
      )
    )

  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
    render(
      containerNode,
      clicksWithVar
    )
  }
}
