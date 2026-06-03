import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object SensorActor:

  import AlarmControllerActor.Notification

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
