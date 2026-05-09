import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*
import scala.util.Failure
import scala.util.Success

object SmartHomeControlSystem:

  val EXIT_DELAY = 60
  val ENTRY_DELAY = 20
  val PIN = "0000"

  object SensorActor:
    import SystemControlActor.Notification.MotionDetected

    enum Signal:
      case DoorWindowSignal(replyTo: ActorRef[MotionDetected])
      case MotionSignal(replyTo: ActorRef[MotionDetected])

    def apply(): Behavior[Signal] = Behaviors.receiveMessage:
      case Signal.DoorWindowSignal(replyTo) =>
        replyTo ! MotionDetected("Detected signal from door or windows")
        Behaviors.same
      case Signal.MotionSignal(replyTo) =>
        replyTo ! MotionDetected("Detected motion signal")
        Behaviors.same

  object KeypadActor:
    import SystemControlActor.Notification.PinInserted

    enum Command:
      case Pin(pin: String, replyTo: ActorRef[Any])
    export Command.*

    def apply(): Behavior[Command] = Behaviors.receiveMessage:
      case Pin(pin, replyTo) if pin == PIN =>
        replyTo ! PinInserted
        Behaviors.same

  object SystemControlActor:
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
        alarm()





