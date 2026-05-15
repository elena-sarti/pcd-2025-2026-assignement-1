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
      case DoorWindowSignal(zone: String, replyTo: ActorRef[Notification])
      case MotionSignal(zone: String, replyTo: ActorRef[Notification])

    def apply(): Behavior[Signal] = Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Signal.DoorWindowSignal(zone, replyTo) =>
          replyTo ! Notification.MotionDetected(zone, "Detected signal from door or windows ")
          Behaviors.same
        case Signal.MotionSignal(zone, replyTo) =>
          replyTo ! Notification.MotionDetected(zone, "Detected motion signal ")
          Behaviors.same

  object KeypadActor:
    import ControlSystemActor.Notification

    enum Command:
      case Pin(pin: String, replyTo: ActorRef[Notification], zonesToArm: String*)
    export Command.*

    def apply(): Behavior[Command] = Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Pin(pin, replyTo, zones*) =>
          context.log.info("Pin inserted...")
          if pin == PIN then {
            context.log.info("Correct pin!")
            replyTo ! Notification.PinInserted(zones*)
          } else context.log.info("Wrong pin - try again")
          Behaviors.same

  object ControlSystemActor:

    enum Notification:
      case MotionDetected(zone: String, msg: String)
      case PinInserted(zonesToArm: String*)
      case TimeoutReached
    export Notification.*

    def apply(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Welcome! Smart home control system ACTIVATED.")
      disarmed()

    def disarmed(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Currently in DISARMED status.")
      Behaviors.receiveMessage:
        case MotionDetected(zone, msg) =>
          context.log.info(msg + s"in $zone!")
          Behaviors.same
        case PinInserted(zones*) =>
          context.log.info("Transitioning to EXIT DELAY status.")
          exitDelay(zones*)
        case _ => Behaviors.same

    def exitDelay(zones: String*): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info(s"Currently in EXIT DELAY status: transitioning to ARMED status in ${EXIT_DELAY} seconds.")
      Behaviors.withTimers:
        timers =>
          timers.startSingleTimer(TimeoutReached, EXIT_DELAY.seconds)
          Behaviors.receiveMessage:
            case TimeoutReached => armed(zones*)
            case MotionDetected(zone, msg) =>
              context.log.info(msg + s"in $zone!")
              Behaviors.same
            case _ => Behaviors.same

    def armed(zones: String*): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("Currently in ARMED status.")
      Behaviors.receiveMessage:
        case PinInserted(_) =>
          disarmed()
        case MotionDetected(zone, msg) if zones.contains(zone) =>
          context.log.info("ATTENTION! " + msg + s"in ${zone}! Transitioning to ENTRY DELAY status.")
          entrydelay(zones*)
        case MotionDetected(zone, msg) =>
          context.log.info(msg + s"in $zone!")
          Behaviors.same
        case _ => Behaviors.same

    def entrydelay(zones: String*): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info(s"Currently in ENTRY DELAY status: transitioning to ALARM status in $ENTRY_DELAY seconds.")
      Behaviors.withTimers:
        timers =>
          timers.startSingleTimer(TimeoutReached, ENTRY_DELAY.seconds)
          Behaviors.receiveMessage:
            case PinInserted(_*) =>
              context.log.info("Correct pin - transitioning to DISARMED status.")
              timers.cancel(TimeoutReached)
              disarmed()
            case TimeoutReached => alarm()
            case MotionDetected(zone, msg) =>
              context.log.info(msg + s"in $zone!")
              Behaviors.same

    def alarm(): Behavior[Notification] = Behaviors.setup: context =>
      context.log.info("ATTENTION! CURRENTLY IN ALARM STATUS!")
      context.log.info("ALARM!")
      Behaviors.receiveMessage:
        case PinInserted(_*) =>
          disarmed()
        case _ => Behaviors.same

  object Guardian:
    import KeypadActor.Command.*
    import SensorActor.Signal.*

    enum Message:
      case SensorMessage(sensor: String, zone: String)
      case KeypadMessage(pin: String, zonesToArm: String*)
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
            case SensorMessage(msg, zone) =>
              sensor ! (if msg == "motion" then MotionSignal(zone, controller) else DoorWindowSignal(zone, controller))
              Behaviors.same
            case KeypadMessage(pin, zones*) =>
              keypad ! Pin(pin, controller, zones*)
              Behaviors.same
          .receiveSignal:
            case (ctx, PostStop) =>
              context.log.info(s"Actor ${ctx.self.path.name} stopping")
              Behaviors.same

@main def runControlSystem(): Unit =
  import SmartHomeControlSystem.Guardian
  import Guardian.Message.*
  val system = ActorSystem(Guardian(), "control-system")

  //dispatcher for the actors of the system - schedules and executes actor message processing and asynchronous tasks
  given ec: ExecutionContext = system.executionContext
  val scheduler = system.scheduler

  //scheduleOnce is not a blocking operation, so the times must increase each time to prevent conflict in the tested sequence of events
  //disarmed status
  scheduler.scheduleOnce(500.millis, () => system ! Guardian.Message.SensorMessage("motion", "living room"))
  scheduler.scheduleOnce(1.second, () => system ! Guardian.Message.KeypadMessage("0001", "perimeter"))

  //exit delay status
  scheduler.scheduleOnce(2.seconds, () => system ! Guardian.Message.KeypadMessage("0000", "perimeter", "living room"))
  scheduler.scheduleOnce(5.seconds, () => system ! Guardian.Message.SensorMessage("motion", "living room"))
  scheduler.scheduleOnce(7.seconds, () => system ! Guardian.Message.SensorMessage("windows", "perimeter"))

  //armed status
  scheduler.scheduleOnce(10.seconds, () => system ! Guardian.Message.SensorMessage("motion", "sleeping zone"))
  scheduler.scheduleOnce(12.seconds, () => system ! Guardian.Message.SensorMessage("door", "sleeping zone"))

  //transition to entry delay status
  scheduler.scheduleOnce(13.seconds, () => system ! Guardian.Message.SensorMessage("windows", "perimeter"))
  scheduler.scheduleOnce(14.seconds, () => system ! Guardian.Message.KeypadMessage("1111"))
  scheduler.scheduleOnce(15.seconds, () => system ! Guardian.Message.SensorMessage("motion", "living room"))

  //transition back to disarmed status
  scheduler.scheduleOnce(16.seconds, () => system ! Guardian.Message.KeypadMessage("0000"))
  scheduler.scheduleOnce(17.seconds, () => system ! Guardian.Message.SensorMessage("motion", "perimeter"))
  scheduler.scheduleOnce(18.seconds, () => system.terminate())





