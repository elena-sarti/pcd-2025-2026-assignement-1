import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object KeypadActor:

  import AlarmControlSystemActor.Notification

  val PIN = "0000"

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
