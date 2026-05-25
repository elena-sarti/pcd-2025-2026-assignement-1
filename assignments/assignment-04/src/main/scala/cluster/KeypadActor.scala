package cluster

import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.cluster.sharding.typed.scaladsl.EntityTypeKey

object KeypadActor:
  val PIN = "0000"

  val Typekey: EntityTypeKey[Pin] = EntityTypeKey[Pin]("Keypad")

  sealed trait Command extends CborSerializable
  final case class Pin(pin: String, replyTo: ActorRef[PinInserted], zonesToArm: String*) extends Command

  final case class PinInserted(zonesToArm: String*) extends CborSerializable

  def apply(): Behavior[Command] = Behaviors.setup: context =>
    Behaviors.receiveMessage:
      case Pin(pin, replyTo, zones*) =>
        context.log.info("Pin inserted...")
        if pin == PIN then {
          context.log.info("Correct pin!")
          replyTo ! PinInserted(zones*)
        } else context.log.info("Wrong pin - try again")
        Behaviors.same