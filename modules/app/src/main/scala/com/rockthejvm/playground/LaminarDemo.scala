package com.rockthejvm.playground

import com.raquo.laminar.api.L.{*, given}
import com.raquo.airstream.ownership.OneTimeOwner
import com.raquo.airstream.timing.PeriodicStream
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

import scala.util.Try

object LaminarDemo {
  private val staticContent: ReactiveHtmlElement[HTMLDivElement] =
    div(
      // modifiers
      styleAttr := "color:red", // <div style="color:red">
      p("This is an app"),
      p("rock the JVM but also JS"),
      p("Lock and Load")
    )

  // REACTIVE VARIABLES
  // 1. EventStream => produce elements, infinite List[A]
  private val ticks: PeriodicStream[Int] =
    EventStream.periodic(1000) // emits every 1000 milliseconds

  private val tickRender = ticks
    .throttle(5000, true)
    .map(number => span(s"Tick: $number"))

  private val timeUpdated: ReactiveHtmlElement[HTMLDivElement] =
    div(
      span("Time since loaded: "),
      child <-- ticks.map(number => s"$number seconds"),
      br(),
      child <-- tickRender
    )

  // 2. EventBus => like EventStream, but you can "push" values into it
  private val clicksBus: EventBus[Int] = EventBus[Int]()
  private val clickEvents: Signal[Int] = clicksBus.events.scanLeft(0)(_ + _)

  private val countingClicks: ReactiveHtmlElement[HTMLDivElement] =
    div(
      span("Click count: "),
      child <-- clickEvents.map(clickCount => s"$clickCount clicks so far"),
      br(),
      button(
        "Click me!",
        // pushes 1 into the clicksBus every time the button is clicked
        onClick.mapTo(1) --> clicksBus
      )
    )

  // 3. Signal => like EventStream, but it also contains "state" or "latest value"
  private val countSignal: OwnedSignal[Int]                       = clicksBus.events
    .scanLeft(0)(_ + _)
    .observe(using new OneTimeOwner(() => ()))
  private val queryBus: EventBus[Unit]                            = EventBus[Unit]()
  private val checkingClicks: ReactiveHtmlElement[HTMLDivElement] =
    div(
      span("Clicks registered since loaded: "),
      child <-- queryBus.events.map(_ => countSignal.now()),
      button(
        onClick.mapTo(1) --> clicksBus,
        "Click me"
      ),
      button(
        onClick.mapTo(()) --> queryBus,
        "Check number of clicks"
      )
    )

  // 4. Var = EventBus (write) + Signal (read + state)
  private val countVar: Var[Int] = Var[Int](0)
  private val clicksWithVar: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks since loaded with Var: "),
    child <-- countVar.signal.map(clickCount => s"$clickCount clicks so far"),
    br(),
    button(
      // procedural
      onClick --> (_ /* mouse event */ -> countVar.set(countVar.now() + 1)),
      "Click me! (procedural style)"
    ),
    button(
      // foldLeft style
      onClick --> countVar.updater((currentValue, event) => currentValue + 1),
      "Click me (foldLeft style)"
    ),
    button(
      // fucntor style
      onClick --> countVar.writer.contramap(event => countVar.now() + 1),
      "Click me (functor style)"
    ),
    button(
      "Reset clicks",
      onClick --> (_ /* mouse event */ -> countVar.set(0))
    )
  )

  def main(args: Array[String]): Unit = {
    val containerNode = dom.document.querySelector("#app")
    render(
      containerNode,
//      timeUpdated,
//      countingClicks
//      checkingClicks
      clicksWithVar
    )
  }
}
