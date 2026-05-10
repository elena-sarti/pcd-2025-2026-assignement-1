import SmartHomeControlSystem.ControlSystemActor.Notification
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*
import scala.util.{Failure, Random, Success}

object SmartHomeControlSystem:
  val EXIT_DELAY = 60
  val ENTRY_DELAY = 20
  val PIN = "0000"

  object SensorActor:
    enum Signal:
      case DoorWindowSignal(replyTo: ActorRef[Notification])
      case MotionSignal(replyTo: ActorRef[Notification])

    def apply(): Behavior[Signal] = Behaviors.receiveMessage:
      case Signal.DoorWindowSignal(replyTo) =>
        replyTo ! Notification.MotionDetected("Detected signal from door or windows")
        Behaviors.same
      case Signal.MotionSignal(replyTo) =>
        replyTo ! Notification.MotionDetected("Detected motion signal")
        Behaviors.same

  object KeypadActor:
    enum Command:
      case Pin(pin: String, replyTo: ActorRef[Notification])
    export Command.*

    def apply(): Behavior[Command] = Behaviors.receiveMessage:
      case Pin(pin, replyTo) if pin == PIN =>
        replyTo ! Notification.PinInserted
        Behaviors.same
      case _ =>
        println("Incorrect pin")
        Behaviors.same

  object ControlSystemActor:
    enum Notification:
      case MotionDetected(msg: String)
      case PinInserted
      case TimeoutReached
    export Notification.*

    def apply(): Behavior[Notification] = disarmed()

    def disarmed(): Behavior[Notification] = Behaviors.receiveMessage:
      case MotionDetected(msg) =>
        println(msg + "in Disarmed status.")
        Behaviors.same
      case PinInserted =>
        println("Correct pin - transitioning to Exit Delay status.")
        exitDelay()

    def exitDelay(): Behavior[Notification] = Behaviors.withTimers:
      timers =>
        println(s"Transitioning to Armed status in ${EXIT_DELAY} seconds.")
        timers.startSingleTimer(TimeoutReached, EXIT_DELAY.seconds)
        Behaviors.receiveMessage:
          case TimeoutReached => armed()
          case MotionDetected(msg) =>
            println(msg + "in Exit Delay status.")
            Behaviors.same
          case _ => Behaviors.same

    def armed(): Behavior[Notification] = Behaviors.receiveMessage:
      case PinInserted => disarmed()
      case MotionDetected(msg) =>
        println("ATTENTION! " + msg + s"in Armed status: transitioning to ALARM status in ${ENTRY_DELAY} seconds.")
        entrydelay()

    def entrydelay(): Behavior[Notification] = Behaviors.withTimers:
      timers =>
        timers.startSingleTimer(TimeoutReached, ENTRY_DELAY.seconds)
        Behaviors.receiveMessage:
          case TimeoutReached => alarm()
          case PinInserted =>
            println("Correct pin - transitioning to Disarmed status.")
            disarmed()
          case MotionDetected(msg) =>
            println(msg + "in Armed status.")
            Behaviors.same

    def alarm(): Behavior[Notification] = Behaviors.receiveMessage:
      case PinInserted => disarmed()
      case _ =>
        println("ALARM!")
        Behaviors.same

  object Guardian:
    import KeypadActor.Command.*
    import SensorActor.Signal.*

    enum Message:
      case SensorMessage(sensor: String)
      case KeypadMessage(pin: String)
    export Message.*

    def apply(): Behavior[Message] = Behaviors.setup:
      context =>
        val controller = context.spawn(ControlSystemActor(), "Hello alarm controller")
        val sensor = context.spawn(SensorActor(), "Hello sensor")
        val keypad = context.spawn(KeypadActor(), "Hello keypad")
        Behaviors.receiveMessage:
          case SensorMessage(msg) =>
            sensor ! (if msg == "motion" then MotionSignal(controller) else DoorWindowSignal(controller))
            Behaviors.same
          case KeypadMessage(pin) =>
            keypad ! Pin(pin, controller)
            Behaviors.same

@main def runControlSystem(): Unit =
  import SmartHomeControlSystem.*
  val system = ActorSystem(Guardian(), "Hello control system")




