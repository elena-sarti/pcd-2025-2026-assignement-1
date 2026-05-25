package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey

object SensorActor:
  
  val TypeKey: EntityTypeKey[Signal] = EntityTypeKey[Signal]("Sensor")  
  
  sealed trait Signal extends CborSerializable
  final case class DoorWindowSignal(zone: String, replyTo: ActorRef[SignalDetected]) extends Signal
  final case class MotionSignal(zone: String, replyTo: ActorRef[SignalDetected]) extends Signal

  final case class SignalDetected(entityId: String, zone: String) extends CborSerializable
  
  def apply(entityId: String): Behavior[Signal] = Behaviors.setup: context =>
    Behaviors.receiveMessage:
      case DoorWindowSignal(zone, replyTo) =>
        replyTo ! SignalDetected(entityId, zone)
        Behaviors.same
      case MotionSignal(zone, replyTo) =>
        replyTo ! SignalDetected(entityId, zone)
        Behaviors.same

