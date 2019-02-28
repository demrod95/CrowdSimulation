package com.kamilkurp

import akka.actor.ActorRef
import com.kamilkurp.ControlScheme.ControlScheme
import org.newdawn.slick.{GameContainer, Image}
import org.newdawn.slick.geom.Vector2f

import scala.util.Random

class Character(val name: String, var room: Room, val controlScheme: ControlScheme, var image: Image) extends Entity {
  override var w: Float = Globals.CHARACTER_SIZE
  override var h: Float = Globals.CHARACTER_SIZE
  override var x: Float = _
  override var y: Float = _
  override var currentVelocityX: Float = 0.0f
  override var currentVelocityY: Float = 0.0f

  var knowsWayOut: Boolean = _

  var controls: (Int, Int, Int, Int) = _

  var timer: Int = 0
  var speed: Float = 0.25f

  var actor: ActorRef = _

  var isFree = false
  while (!isFree) {
    x = Random.nextInt(room.w - w.toInt)
    y = Random.nextInt(room.h - h.toInt)

    val collisionDetails = Globals.manageCollisions(room, this)
    if (!collisionDetails.colX || !collisionDetails.colY) {
      isFree = true
    }
  }

  knowsWayOut = Random.nextInt(100) < 30

  def this(name: String, room: Room, controlScheme: ControlScheme, controls: (Int, Int, Int, Int), image: Image) {
    this(name, room, controlScheme, image)
    this.controls = controls


  }

  def update(gc: GameContainer, delta: Int): Unit = {
    timer = timer + delta

    if (controlScheme == ControlScheme.Random) {

      //      else {
      val door = this.room.evacuationDoor

      if (knowsWayOut) {
        if (door != null) {
          val normalVector = new Vector2f(door.x - this.x, door.y - this.y)
          normalVector.normalise()

          currentVelocityX = normalVector.x
          currentVelocityY = normalVector.y

          //            if (this.x > door.x) {
          //              currentVelocityX = -speed
          //            }
          //            else {
          //              currentVelocityX = speed
          //            }
          //            if (this.y > door.y) {
          //              currentVelocityY = -speed
          //            }
          //            else {
          //              currentVelocityY = speed
          //            }
        }
      }
      else {
        if (timer > 500) {
          val inPlace = Random.nextInt(100) < 30

          timer = 0
          if (inPlace) {
            currentVelocityX = 0
            currentVelocityY = 0
          }
          else {
            currentVelocityX = (Random.nextInt(3) - 1) * speed
            currentVelocityY = (Random.nextInt(3) - 1) * speed
          }

        }
      }




      //  }

      //      }

      val collisionDetails = Globals.manageCollisions(room, this)

      if (!collisionDetails.colX) {
        this.x += currentVelocityX
      }
      if (!collisionDetails.colY) {
        this.y += currentVelocityY
      }
    }
    else if (controlScheme == ControlScheme.Manual) {
      val offset = speed * delta
      val oldX = x
      val oldY = y

      var moved = false

      if (gc.getInput.isKeyDown(controls._1)) {
        x += -offset
        moved = true
      }
      if (gc.getInput.isKeyDown(controls._2)) {
        x += offset
        moved = true
      }
      if (gc.getInput.isKeyDown(controls._3)) {
        y += -offset
        moved = true
      }
      if (gc.getInput.isKeyDown(controls._4)) {
        y += offset
        moved = true
      }

      val collisionDetails = Globals.manageCollisions(room, this)
      if (collisionDetails.colX) x = oldX
      if (collisionDetails.colY) y = oldY

      if (moved) {
        CameraView.x = room.x + x - Globals.WINDOW_X / 2 + w / 2
        CameraView.y = room.y + y - Globals.WINDOW_Y / 2 + h / 2
      }
    }
  }


  override def onCollision(entity: Entity): Unit = {
    //println("this character " + name + " collided with " + entity.name)
  }

  def changeRoom(newRoom: Room, newX: Float, newY: Float): Unit = {
    room.removeCharacter(this)
    newRoom.addCharacter(this)

    room = newRoom
    x = newX
    y = newY
  }

  def setActor(actor: ActorRef): Unit = {
    this.actor = actor
  }
}