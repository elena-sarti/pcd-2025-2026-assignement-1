package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey

object Sensor:
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Sensor")

  sealed trait Command extends CborSerializable
  final case class Signal(zone: String) extends Command
  
  def apply(entityId: String): Behavior[Command] = Behaviors.setup: context =>
    val sharding = ClusterSharding(context.system)
    val sensorType = if entityId.contains("motion") then "MOTION" else if entityId.contains("door") then "DOOR" else "WINDOW"
    Behaviors.receiveMessage:
      case Signal(zone) =>
        context.log.info(s"$sensorType sensor in zone $zone triggered!")
        val alarmControlSystemActor = sharding.entityRefFor(AlarmControlSystem.TypeKey, "Alarm-control-unit")
        alarmControlSystemActor ! AlarmControlSystem.MotionDetected(zone, entityId)
        Behaviors.same
