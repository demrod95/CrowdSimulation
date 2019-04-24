package com.kamilkurp.agent

import akka.actor.{Actor, ActorLogging}
import com.kamilkurp.behavior._
import com.kamilkurp.building.Door
import com.kamilkurp.entity.Entity
import com.kamilkurp.flame.Flames
import org.jgrapht.graph.DefaultWeightedEdge
import org.newdawn.slick.geom.Vector2f

import scala.collection.mutable.ListBuffer
import scala.util.Random

case class AgentWithinVision(entity: Entity, distance: Float)

case class AgentEnteredDoor(agent: Agent, door: Door, locationX: Float, locationY: Float)

case class AgentLeading(agent: Agent, locationX: Float, locationY: Float)

case class FireWithinVision(flames: Flames, locationX: Float, locationY: Float)


class AgentActor(val name: String, val agent: Agent) extends Actor with ActorLogging {

  val char: Agent = agent


  override def receive: Receive = {
    case AgentWithinVision(that: Agent, distance: Float) =>

      if (!agent.goTowardsDoor) {
        if (agent.behavior.currentBehavior != LeaderBehavior.name) {
          if (that.behavior.currentBehavior == LeaderBehavior.name) {
            agent.behavior.follow(that, that.shape.getCenterX, that.shape.getCenterY, 120)
            agent.followManager.lostSightOfFollowedEntity = false
            agent.followManager.lastSeenFollowedEntityTimer.reset()
            agent.followManager.lastSeenFollowedEntityTimer.start()
          }
        }
      }


    case AgentEnteredDoor(that, door, locationX, locationY) =>
      if (that.behavior.currentBehavior == LeaderBehavior.name) {
        agent.doorToEnter = door
        agent.behavior.follow(that, locationX, locationY, 0)
        agent.followManager.followTimer.start()
        agent.followManager.followTimer.reset()
        agent.goTowardsDoor = true
      }

    case AgentLeading(entity, locationX, locationY) =>
      if (agent.behavior.currentBehavior == IdleBehavior.name || agent.behavior.currentBehavior == SearchExitBehavior.name || agent.behavior.currentBehavior == FollowBehavior.name) {

        val normalVector = new Vector2f(locationX - agent.shape.getCenterX, locationY - agent.shape.getCenterY)
        normalVector.normalise()

        agent.walkAngle = normalVector.getTheta.floatValue()
        //agent.viewAngle = normalVector.getTheta.floatValue()
      }

    case FireWithinVision(flames, locationX, locationY) =>



//      println(agent.name + " saw fire!")

      //agent.behaviorManager.setBehavior(AvoidFireBehavior.name)

      val edgeIter = agent.weightedGraph.edgeSet().iterator()

      var toRemove: ListBuffer[DefaultWeightedEdge] = new ListBuffer[DefaultWeightedEdge]()
      while(edgeIter.hasNext) {
        val edge = edgeIter.next()

        if(agent.doorToEnter != null) {
          if (agent.weightedGraph.getEdgeSource(edge) == agent.room && agent.weightedGraph.getEdgeTarget(edge) == agent.doorToEnter.leadingToDoor.room) {
//            println("removing edge " + agent.weightedGraph.getEdgeSource(edge).name + " " + agent.weightedGraph.getEdgeTarget(edge).name)
            toRemove += edge
          }
        }


      }

      for (edge <- toRemove) {
        if (agent.weightedGraph.getEdgeTarget(edge).meetPointList.isEmpty) {
          if (agent.debug) {
            println("removed edge " + agent.weightedGraph.getEdgeSource(edge) + " " + agent.weightedGraph.getEdgeTarget(edge))
          }
          agent.weightedGraph.removeEdge(edge)

        }
      }

      agent.doorToEnter = agent.findDoorToEnterNext()







  }
}
