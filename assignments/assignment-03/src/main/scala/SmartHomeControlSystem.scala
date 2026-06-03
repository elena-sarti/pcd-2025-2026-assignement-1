import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.scaladsl.*

import scala.concurrent.duration.DurationInt

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
          .supervise(AlarmControllerActor())
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

      //starting simulation using timers
      Behaviors.withTimers: timers =>
        // Disarmed status
        timers.startSingleTimer(SensorMessage("motion", "living room"), 500.millis)
        timers.startSingleTimer(KeypadMessage("0001", "perimeter"), 1.second)
        // Exit delay status
        timers.startSingleTimer(KeypadMessage("0000", "perimeter"), 2.seconds)
        timers.startSingleTimer(SensorMessage("motion", "living room"), 5.seconds)
        timers.startSingleTimer(SensorMessage("windows", "perimeter"), 7.seconds)
        // Armed status
        timers.startSingleTimer(SensorMessage("motion", "sleeping zone"), 10.seconds)
        timers.startSingleTimer(SensorMessage("door", "sleeping zone"), 12.seconds)
        // Transition to entry delay status
        timers.startSingleTimer(SensorMessage("windows", "perimeter"), 13.seconds)
        timers.startSingleTimer(KeypadMessage("1111"), 14.seconds)
        timers.startSingleTimer(SensorMessage("motion", "living room"), 15.seconds)
        // Transition back to disarmed status
        timers.startSingleTimer(KeypadMessage("0000"), 16.seconds)
        timers.startSingleTimer(SensorMessage("motion", "perimeter"), 17.seconds)

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
  val system = ActorSystem(Guardian(), "control-system")

