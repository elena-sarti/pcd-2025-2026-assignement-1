package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey

object Keypad:
  val PIN = "0000"
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Keypad")

  sealed trait Command extends CborSerializable
  final case class Pin(pin: String, zonesToArm: List[String]) extends Command

  def apply(entityId: String): Behavior[Command] = Behaviors.setup: context =>
    //initializing sharding on this node
    val sharding = ClusterSharding(context.system)
    Behaviors.receiveMessage:
      case Pin(pin, zones) =>
        context.log.info("Pin inserted...")
        if pin == PIN then
          context.log.info("Correct pin!")
          //retrieving the alarm system ref through the sharding
          val alarmControlSystemActor = sharding.entityRefFor(AlarmControlSystem.TypeKey, "Alarm-control-unit")
          alarmControlSystemActor ! AlarmControlSystem.PinInserted(zones)
        else context.log.info("Wrong pin - try again")
        Behaviors.same