package com.kamilkurp.agent

import com.kamilkurp.behaviors._

import scala.collection.mutable

trait BehaviorManager {
  this: Agent =>

  val behaviorMap: mutable.HashMap[String, Behavior] = mutable.HashMap.empty[String, Behavior]
  var currentBehavior: String = _

  behaviorMap += ("follow" -> new FollowBehavior(this))
  behaviorMap += ("idle" -> new IdleBehavior(this))
  behaviorMap += ("leader" -> new LeaderBehavior(this))
  behaviorMap += ("holdMeetPoint" -> new HoldMeetPointBehavior(this))

  def setBehavior(behaviorName: String): Unit = {
    currentBehavior = behaviorName
    behaviorMap(behaviorName).init()
  }

  def follow(character: Agent, posX: Float, posY: Float, atDistance: Float): Unit = {
    getBehavior(currentBehavior).follow(character, posX, posY, atDistance)
  }

  def getBehavior(behaviorName: String): Behavior = behaviorMap(behaviorName)
}
