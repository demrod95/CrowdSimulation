package com.kamilkurp.flame

import com.kamilkurp.building.{Floor, Room}
import com.kamilkurp.util.{Configuration, Globals, Timer}
import org.newdawn.slick.Image

import scala.collection.mutable.ListBuffer
import scala.util.Random

class FlamesManager() {
  var flamesPropagationTimer: Timer = _
  var flamesImage: Image = _
  var flamesList: ListBuffer[Flames] = _

  def init(floorList: ListBuffer[Floor]): Unit = {
    flamesPropagationTimer = new Timer((10000f / Configuration.flamePropagationSpeed).toInt)
    flamesPropagationTimer.start()
    flamesImage = new Image(Configuration.FLAMES_IMAGE_LOCATION)
    flamesList = new ListBuffer[Flames]()


    //    for (i <- 0 until 7) {
    //      addRandomFlame(roomList)
    //    }

    val room: Room = floorList(0).roomList.filter(room => room.name == "roomM").head

    val flames = new Flames(room, Random.nextInt(room.w - flamesImage.getWidth), Random.nextInt(room.h - flamesImage.getHeight), flamesImage)

    room.flamesList += flames
    flamesList += flames

  }

  def addRandomFlame(roomList: ListBuffer[Room]): Unit = {
    val randomRoom = Random.nextInt(roomList.length)
    val room: Room = roomList(randomRoom)

    val flames = new Flames(room, Random.nextInt(room.w - flamesImage.getWidth), Random.nextInt(room.h - flamesImage.getHeight), flamesImage)

    room.flamesList += flames
    flamesList += flames
  }

  def handleFlamePropagation(): Unit = {
    if (flamesPropagationTimer.timedOut()) {
      flamesPropagationTimer.reset()

      val length = flamesList.length

      for (x <- 0 until length) {

        val flames = flamesList(x)

        if (!flames.dontUpdate) {


          var newFlames: Flames = null


          var pairs: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]()

          for (i <- -1 to 1) {
            for (j <- -1 to 1) {
              if (!(i == 0 && j == 0)) {
                pairs.append((i, j))
              }
            }
          }

          val shuffledPairs = Random.shuffle(pairs)

          var foundSpot = false
          for (pair <- shuffledPairs) {


            if (!foundSpot) {


              newFlames = new Flames(flames.currentRoom, flames.shape.getX, flames.shape.getY, flames.image)

              newFlames.shape.setX(flames.shape.getX.toInt + (flamesImage.getWidth + 5) * pair._1)
              newFlames.shape.setY(flames.shape.getY.toInt + (flamesImage.getHeight + 5) * pair._2)

              var isFree = true

              newFlames.currentRoom.flamesList.foreach(that => {
                if (newFlames.shape.getX < 0 || newFlames.shape.getX > newFlames.currentRoom.w - newFlames.shape.getWidth) isFree = false
                if (newFlames.shape.getY < 0 || newFlames.shape.getY > newFlames.currentRoom.h - newFlames.shape.getHeight) isFree = false

                if (Globals.intersects(newFlames, that.shape.getX, that.shape.getY, that.shape.getWidth, that.shape.getHeight, 0, 0)) {
                  isFree = false
                }
              })

              if (isFree) {
                flames.currentRoom.flamesList += newFlames
                flamesList += newFlames

                foundSpot = true

                newFlames.currentRoom.doorList.foreach(that => {
                  if (Globals.intersects(newFlames, that.shape.getX, that.shape.getY, that.shape.getWidth, that.shape.getHeight, 0, 0)) {
                    var foundNewRoomSpot = false
                    for (i <- -1 to 1) {
                      for (j <- -1 to 1) {
                        if (!foundNewRoomSpot) {
                          val leadingToDoor = that.leadingToDoor
                          val spotX = leadingToDoor.posX + i * 60
                          val spotY = leadingToDoor.posY + j * 60

                          if (!Globals.isRectTraversable(leadingToDoor.currentRoom, spotX - 10, spotY - 10, newFlames.shape.getWidth + 20, newFlames.shape.getHeight + 20, newFlames)) {

                            val newRoom: Room = that.leadingToDoor.currentRoom

                            val newRoomFlames = new Flames(newRoom, spotX, spotY, flamesImage)
                            newRoom.flamesList += newRoomFlames
                            flamesList += newRoomFlames

                            foundNewRoomSpot = true
                          }
                        }

                      }
                    }
                  }
                })

              }
            }

          }

          if (!foundSpot) {
            flames.dontUpdate = true
          }


        }
      }
    }
  }
}
