import SmartHomeControlSystem.EXIT_DELAY
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

object SmartHomeControlSystem:
  val EXIT_DELAY = 6
  val ENTRY_DELAY = 2
  val PIN = "0000"

  object SensorActor:
    import ControlSystemActor.Notification
    enum Signal:
      case DoorWindowSignal(replyTo: ActorRef[Notification])
      case MotionSignal(replyTo: ActorRef[Notification])

    def apply(): Behavior[Signal] = Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Signal.DoorWindowSignal(replyTo) =>
          replyTo ! Notification.MotionDetected("Detected signal from door or windows!")
          Behaviors.same
        case Signal.MotionSignal(replyTo) =>
          replyTo ! Notification.MotionDetected("Detected motion signal!")
          Behaviors.same

  object KeypadActor:
    import ControlSystemActor.Notification
    enum Command:
      case Pin(pin: String, replyTo: ActorRef[Notification])
    export Command.*

    def apply(): Behavior[Command] = Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Pin(pin, replyTo) =>
          context.log.info("Pin inserted...")
          if pin == PIN then replyTo ! Notification.PinInserted else context.log.info("Wrong pin - try again")
          Behaviors.same

  object ControlSystemActor:
    enum Notification:
      case MotionDetected(msg: String)
      case PinInserted
      case TimeoutReached
    export Notification.*

    def apply(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Welcome! Smart home control system ACTIVATED.")
      disarmed()

    def disarmed(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Currently in DISARMED status.")
      Behaviors.receiveMessage:
        case MotionDetected(msg) =>
          context.log.info(msg)
          Behaviors.same
        case PinInserted =>
          context.log.info("Correct pin - transitioning to EXIT DELAY status.")
          exitDelay()

    def exitDelay(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info(s"Currently in EXIT DELAY status: transitioning to ARMED status in ${EXIT_DELAY} seconds.")
      Behaviors.withTimers:
        timers =>
          timers.startSingleTimer(TimeoutReached, EXIT_DELAY.seconds)
          Behaviors.receiveMessage:
            case TimeoutReached => armed()
            case MotionDetected(msg) =>
              context.log.info(msg)
              Behaviors.same
            case _ => Behaviors.same

    def armed(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Currently in ARMED status.")
      Behaviors.receiveMessage:
        case PinInserted => disarmed()
        case MotionDetected(msg) =>
          context.log.info("ATTENTION! " + msg + " Transitioning to ENTRY DELAY status.")
          entrydelay()

    def entrydelay(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info(s"Currently in ENTRY DELAY status: transitioning to ALARM status in $ENTRY_DELAY seconds.")
      Behaviors.withTimers:
        timers =>
          timers.startSingleTimer(TimeoutReached, ENTRY_DELAY.seconds)
          Behaviors.receiveMessage:
            case PinInserted =>
              context.log.info("Correct pin - transitioning to DISARMED status.")
              timers.cancel(TimeoutReached)
              disarmed()
            case TimeoutReached => alarm()
            case MotionDetected(msg) =>
              context.log.info(msg)
              Behaviors.same

    def alarm(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("ATTENTION! CURRENTLY IN ALARM STATUS!")
      Behaviors.receiveMessage:
        case PinInserted => disarmed()
        case _ =>
          context.log.info("ALARM!")
          Behaviors.same

  object Guardian:
    import KeypadActor.Command.*
    import SensorActor.Signal.*

    enum Message:
      case SensorMessage(sensor: String)
      case KeypadMessage(pin: String)
    export Message.*

    def apply() : Behavior[Message] = Behaviors.setup:
      context =>
        val controller = context.spawn(
          Behaviors
            .supervise(ControlSystemActor())
            .onFailure[Exception](SupervisorStrategy.restart),
            "alarm-controller",
        )
        val sensor = context.spawn(
          Behaviors
            .supervise(SensorActor())
            .onFailure[Exception](SupervisorStrategy.restart),
            "sensor",
        )
        val keypad = context.spawn(
          Behaviors
            .supervise(KeypadActor())
            .onFailure(SupervisorStrategy.restart),
            "keypad",
        )
        Behaviors
          .receiveMessage[Message]:
            case SensorMessage(msg) =>
              sensor ! (if msg == "motion" then MotionSignal(controller) else DoorWindowSignal(controller))
              Behaviors.same
            case KeypadMessage(pin) =>
              keypad ! Pin(pin, controller)
              Behaviors.same
          .receiveSignal:
            case (ctx, PostStop) =>
              context.log.info(s"Actor ${ctx.self.path.name} stopping")
              Behaviors.same

@main def runControlSystem(): Unit =
  import SmartHomeControlSystem.Guardian
  import Guardian.Message.*
  val system = ActorSystem(Guardian(), "control-system")

  given ec: ExecutionContext = system.executionContext
  val scheduler = system.scheduler

  system ! SensorMessage("motion")
  system ! SensorMessage("windows")
  system ! KeypadMessage("0001")
  scheduler.scheduleOnce(2.seconds, () => system ! KeypadMessage("0000"))
  system ! SensorMessage("motion")
  scheduler.scheduleOnce((EXIT_DELAY + 1).seconds, () => system ! SensorMessage("door"))
  system ! KeypadMessage("0001")
  system ! KeypadMessage("0000")
  system ! SensorMessage("motion")
  scheduler.scheduleOnce(1.seconds, () => system ! KeypadMessage("0000"))





