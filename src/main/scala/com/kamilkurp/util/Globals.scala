package com.kamilkurp.util

import java.util.ConcurrentModificationException

import com.kamilkurp.building.Room
import com.kamilkurp.entity.Entity
import org.jgrapht.graph.{DefaultDirectedWeightedGraph, DefaultWeightedEdge}

object Globals {
  val AGENT_SIZE: Int = 40
  val WINDOW_X: Int = 800
  val WINDOW_Y: Int = 600
  val SCALE_X: Float = 0.5f
  val SCALE_Y: Float = 0.5f

  def copyGraph(graph: DefaultDirectedWeightedGraph[Room, DefaultWeightedEdge]): DefaultDirectedWeightedGraph[Room, DefaultWeightedEdge] = {
    val newGraph = new DefaultDirectedWeightedGraph[Room, DefaultWeightedEdge](classOf[DefaultWeightedEdge])

    var vertexArray: Array[AnyRef] = null

    while (vertexArray == null) {
      try {
        vertexArray = graph.vertexSet().toArray
      } catch {
        case _: ConcurrentModificationException =>
          vertexArray = null
      }
    }

    for (ref <- vertexArray) {
      val room = ref.asInstanceOf[Room]
      newGraph.addVertex(room)
    }


    var edgeArray: Array[AnyRef] = null

    while (edgeArray == null) {
      try {
        edgeArray = graph.edgeSet().toArray
      } catch {
        case _: ConcurrentModificationException =>
          edgeArray = null
      }
    }

    for (edgeRef <- edgeArray) {
      val edge = edgeRef.asInstanceOf[DefaultWeightedEdge]
      val weightedEdge: DefaultWeightedEdge = newGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge))
      newGraph.setEdgeWeight(weightedEdge, 1.0f)

    }

    newGraph
  }

  def manageCollisions(room: Room, entity: Entity, collisionVelocityX: Float, collisionVelocityY: Float): CollisionDetails = {

    val collisionDetails: CollisionDetails = new CollisionDetails(false, false)

    if (entity.shape.getX + collisionVelocityX < 0 || entity.shape.getX + collisionVelocityX > room.w - entity.shape.getWidth) collisionDetails.colX = true
    if (entity.shape.getY + collisionVelocityY < 0 || entity.shape.getY + collisionVelocityY > room.h - entity.shape.getHeight) collisionDetails.colY = true

    room.agentList.filter(agent => agent != entity).foreach(agent => {

      var collided = false

      if (intersectsX(entity, agent.shape.getX, agent.shape.getY, agent.shape.getWidth, agent.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colX = true
        collided = true
      }
      if (intersectsY(entity, agent.shape.getX, agent.shape.getY, agent.shape.getWidth, agent.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colY = true
        collided = true
      }

      if (collided) {
        entity.onCollision(agent)
        agent.onCollision(entity)
      }
    })

    room.meetPointList.foreach(meetPoint => {

      var collided = false

      if (intersectsX(entity, meetPoint.shape.getX, meetPoint.shape.getY, meetPoint.shape.getWidth, meetPoint.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colX = true
        collided = true
      }
      if (intersectsY(entity, meetPoint.shape.getX, meetPoint.shape.getY, meetPoint.shape.getWidth, meetPoint.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colY = true
        collided = true
      }

      if (collided) {
        entity.onCollision(meetPoint)
        meetPoint.onCollision(entity)
      }
    })

    room.doorList.foreach(door => {
      if (intersects(entity, door.shape.getX, door.shape.getY, door.shape.getWidth, door.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        entity.onCollision(door)
        door.onCollision(entity)
      }
    })

    room.flamesList.foreach(flames => {

      var collided = false

      if (intersectsX(entity, flames.shape.getX, flames.shape.getY, flames.shape.getWidth, flames.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colX = true
        collided = true

      }
      if (intersectsY(entity, flames.shape.getX, flames.shape.getY, flames.shape.getWidth, flames.shape.getHeight, collisionVelocityX, collisionVelocityY)) {
        collisionDetails.colY = true
        collided = true

      }

      if (collided) {
        entity.onCollision(flames)
        flames.onCollision(entity)
      }
    })

    collisionDetails
  }

  def isRectOccupied(room: Room, x: Float, y: Float, w: Float, h: Float, entity: Entity): Boolean = {
    var occupied = false

    if (x < 0 || x > room.w - w) occupied = true
    if (y < 0 || y > room.h - h) occupied = true

    room.agentList.foreach(agent => {
      if (intersects(agent, x, y, w, h, 0, 0)) {

        occupied = true
      }
    })

    room.doorList.foreach(door => {
      if (intersects(door, x, y, w, h, 0, 0)) {
        occupied = true
      }
    })

    occupied
  }

  def isRectTraversable(room: Room, x: Float, y: Float, w: Float, h: Float, entity: Entity): Boolean = {
    var occupied = false

    if (x < 0 || x > room.w - w) occupied = true
    if (y < 0 || y > room.h - h) occupied = true

    room.doorList.foreach(door => {
      if (intersects(door, x, y, w, h, 0, 0)) {
        occupied = true
      }
    })

    occupied
  }

  def intersects(entity: Entity, thatX: Float, thatY: Float, thatW: Float, thatH: Float, collisionVelocityX: Float, collisionVelocityY: Float): Boolean = {

    if (intersectsX(entity, thatX, thatY, thatW, thatH, collisionVelocityX, collisionVelocityY) || intersectsY(entity, thatX, thatY, thatW, thatH, collisionVelocityX, collisionVelocityY)) true
    else false

  }

  private def intersectsX(entity: Entity, thatX: Float, thatY: Float, thatW: Float, thatH: Float, collisionVelocityX: Float, collisionVelocityY: Float): Boolean = {

    if (entity.shape.getX + collisionVelocityX < thatX + thatW &&
      entity.shape.getX + collisionVelocityX + entity.shape.getWidth > thatX &&
      entity.shape.getY < thatY + thatH &&
      entity.shape.getHeight + entity.shape.getY > thatY) true
    else false

  }

  private def intersectsY(entity: Entity, thatX: Float, thatY: Float, thatW: Float, thatH: Float, collisionVelocityX: Float, collisionVelocityY: Float): Boolean = {

    if (entity.shape.getX < thatX + thatW &&
      entity.shape.getX + entity.shape.getWidth > thatX &&
      entity.shape.getY + collisionVelocityY < thatY + thatH &&
      entity.shape.getHeight + entity.shape.getY + collisionVelocityY > thatY) true
    else false

  }

  class CollisionDetails(var colX: Boolean, var colY: Boolean)

}
