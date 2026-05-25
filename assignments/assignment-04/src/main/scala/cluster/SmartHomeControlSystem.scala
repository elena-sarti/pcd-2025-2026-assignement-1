package cluster

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.SupervisorStrategy
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}

import scala.concurrent.duration.DurationInt

object SmartHomeControlSystem:
  sealed trait Command
  case object InsertPin extends Command
  case object TriggerUnarmedZone extends Command
  case object TriggerArmedZone extends Command
  case object UnlockSystem extends Command

  def rootBehavior(): Behavior[Command] = Behaviors.setup: context =>
    val sharding = ClusterSharding(context.system)
    val supervisorStrategy = SupervisorStrategy.restart

    val sensorEntity = sharding.init(Entity(typeKey = Sensor.TypeKey): entityContext =>
      Sensor(entityContext.entityId)
    )

    val keypadEntity = sharding.init(Entity(typeKey = Keypad.TypeKey): entityContext =>
      Keypad(entityContext.entityId)
    )

    val alarmControlSystemEntity = sharding.init(Entity(typeKey = AlarmControlSystem.TypeKey): entityContext =>
      Behaviors
        .supervise(AlarmControlSystem(entityContext.entityId))
        .onFailure[Exception](supervisorStrategy)
    )

    //Running a simulation on the node different from the seed nodes
    val currentPort = context.system.address.port.getOrElse(0)
    if currentPort != 25251 && currentPort != 25252 then
      context.log.info(s"[TEST NODE $currentPort] Starting timed simulation in 5 seconds...")
      Behaviors.withTimers: timers =>
        // After 1s, the user enters the pin to unlock the system
        timers.startSingleTimer(UnlockSystem, 1.seconds)
        // After 5s, the user enters the pin to arm the system
        timers.startSingleTimer(InsertPin, 5.seconds)
        // After 8s, a sensor detects motion in an unprotected zone (nothing should happen)
        timers.startSingleTimer(TriggerUnarmedZone, 8.seconds)
        // After 15s (after the 6s exit delay expires), a sensor triggers the armed zone
        timers.startSingleTimer(TriggerArmedZone, 15.seconds)

        Behaviors.receiveMessage:
          case UnlockSystem =>
            context.log.info("[SIMULATION] -> Action: Submitting correct PIN to enter disarmed status")
            val keypadRef = sharding.entityRefFor(Keypad.TypeKey, "keypad-recovery")
            keypadRef ! Keypad.Pin("0000", List())
            Behaviors.same
          case InsertPin =>
            context.log.info("[SIMULATION] -> Action: Submitting correct PIN to arm 'Day-Zone'")
            val keypadRef = sharding.entityRefFor(Keypad.TypeKey, "keypad-entrance")
            keypadRef ! Keypad.Pin("0001", List("LIVING ROOM"))
            Behaviors.same
          case TriggerUnarmedZone =>
            context.log.info("[SIMULATION] -> Action: Motion detected in Kitchen ('Kitchen-Zone' is not armed)")
            val sensorRef = sharding.entityRefFor(Sensor.TypeKey, "MOTION")
            sensorRef ! Sensor.Signal("PERIMETER")
            Behaviors.same
          case TriggerArmedZone =>
            context.log.info("[SIMULATION] -> Action: Motion detected in Living Room ('Day-Zone' is armed!)")
            val sensorRef = sharding.entityRefFor(Sensor.TypeKey, "DOOR")
            sensorRef ! Sensor.Signal("LIVING ROOM")
            Behaviors.same
    else
      // Seed nodes (on ports 25251 and 25252) just keep running to host shards and handle routing
      Behaviors.empty


  def main(args: Array[String]): Unit =
    val ports =
      if args.nonEmpty then args.toSeq.map(_.toInt)
      else sys.env.get("CLUSTER_PORT").flatMap(_.toIntOption).map(Seq(_)).getOrElse(Seq(25251, 25252, 0))

    ports.foreach(startup)

  private def startup(port: Int): Unit =
    val config = ConfigFactory.parseString(
      s"""
       pekko.remote.artery.canonical.port = $port
       """).withFallback(ConfigFactory.load("application-sharding.conf"))

    val _ = ActorSystem(rootBehavior(), "ClusterSystem", config)

