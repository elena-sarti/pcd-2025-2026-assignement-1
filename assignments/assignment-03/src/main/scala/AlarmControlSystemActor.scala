import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.duration.DurationInt

object AlarmControlSystemActor:

  val EXIT_DELAY = 6
  val ENTRY_DELAY = 2

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