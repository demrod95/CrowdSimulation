package com.kamilkurp.agent

import akka.actor.{Actor, ActorLogging}
import com.kamilkurp.behavior._
import com.kamilkurp.building.Room
import com.kamilkurp.entity.Entity
import com.kamilkurp.flame.Flames

import scala.collection.mutable

abstract class AgentMessage

case class AgentWithinVision(entity: Entity) extends AgentMessage

case class AgentLeading(agent: Agent) extends AgentMessage

case class FireWithinVision() extends AgentMessage

case class FireLocationInfo(fireLocations: mutable.Set[Room]) extends AgentMessage


class AgentActor(val name: String, val agent: Agent) extends Actor with ActorLogging {

  val char: Agent = agent


  override def receive: Receive = {
    case AgentWithinVision(that: Agent) =>

      if (agent.currentBehavior.name != LeaderBehavior.name) {
        if (that.currentBehavior.name == LeaderBehavior.name) {
          if (agent.followedAgent == null) {
            agent.followLeader(that)
          }
        }
      }

    case AgentLeading(that) =>
      if (agent.currentBehavior.name == IdleBehavior.name || agent.currentBehavior.name == SearchExitBehavior.name || agent.currentBehavior.name == FollowBehavior.name) {
        if (agent.currentBehavior.name != LeaderBehavior.name) {
          if (that.currentBehavior.name == LeaderBehavior.name) {
            if (agent.followedAgent == null) {
              agent.followLeader(that)
            }
          }
        }

      }

    case FireWithinVision() =>
      agent.spatialModule.onSpottingFire()

    case FireLocationInfo(fireLocations: mutable.Set[Room]) =>
      agent.spatialModule.onReceiveFireLocationInfo(fireLocations)

  }
}
