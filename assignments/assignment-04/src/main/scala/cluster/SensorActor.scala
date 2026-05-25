package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey

object SensorActor:
  val TypeKey: EntityTypeKey[Signal] = EntityTypeKey[Signal]("Sensor")
  
  sealed trait Command extends CborSerializable
  final case class Signal(zone: String) extends Command
  
  def apply(entityId: String): Behavior[Signal] = Behaviors.setup: context =>
    val sharding = ClusterSharding(context.system)
    Behaviors.receiveMessage:
      case Signal(zone) =>
        context.log.info(s"Sensor $entityId in zone $zone triggered!")
        val alarmControlSystemActor = sharding.entityRefFor(AlarmControlSystem.TypeKey, "Alarm-control-unit")
        alarmControlSystemActor ! AlarmControlSystem.MotionDetected(zone, entityId)
        Behaviors.same
