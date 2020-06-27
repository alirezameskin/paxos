package paxos.demo

import java.util.concurrent.TimeUnit

import cats.Traverse
import cats.effect.{IO, Timer}
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import paxos.{Message, Messenger, Node, Peer}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class IOMessenger[I, V](implicit val timer: Timer[IO], val logger: Logger[IO]) extends Messenger[IO, I, V] {
  val random = new Random()
  val nodes  = scala.collection.mutable.HashMap.empty[Peer, Node[IO, I, V]]

  val ec          = scala.concurrent.ExecutionContext.global
  implicit val cs = IO.contextShift(ec)

  def addNode(node: Node[IO, I, V]*) =
    node
      .foreach { n =>
        nodes.put(n.peer, n)
      }
      .pure[IO]

  def unicast(from: Peer, to: Peer, msg: Message[I, V]): IO[Unit] = {
    val dd = IO.shift(cs) *> IO.sleep(
      FiniteDuration(random.nextInt(900), TimeUnit.MILLISECONDS)
    ) *> IO {
      nodes.get(to).map { n =>
        n.receiveMessage(from, msg).unsafeRunSync()
      }
    }.void

    dd.start.void
  }

  def broadcast(from: Peer, msg: Message[I, V]): IO[Unit] = {
    val re = IO.shift(cs) *> Traverse[List].sequence {
      nodes.map {
        case (_, n) =>
          IO.sleep(FiniteDuration(random.nextInt(900), TimeUnit.MILLISECONDS)) *> n
            .receiveMessage(from, msg)
      }.toList
    }

    re.start.void

  }

}
