package com.kamilkurp.entity

import com.kamilkurp.building.{Door, Room}
import org.newdawn.slick.{Graphics, Image}
import org.newdawn.slick.geom.{Rectangle, Shape}

class Flames(var room: Room, var posX: Float, var posY: Float, var image: Image) extends Entity {

  override val name: String = "fire"
  override var currentVelocityX: Float = 0
  override var currentVelocityY: Float = 0
  override var shape: Shape = _

  var dontUpdate: Boolean = false



  shape = new Rectangle(posX, posY, 40, 55)

  override def onCollision(entity: Entity): Unit = {
//    println("collided " + shape.getX + " " + shape.getY + " with " + entity.shape.getY + " " + entity.shape.getY)
    //println("collided with " + entity.getClass)
  }

  override def changeRoom(entryDoor: Door, newX: Float, newY: Float): Unit = {

  }

  def draw(g: Graphics, offsetX: Float, offsetY: Float): Unit = {
    g.drawImage(this.image, room.x + shape.getX - offsetX, room.y + shape.getY - offsetY)
  }




}
