package paxos.demo

import java.util.concurrent.TimeUnit

import cats.Traverse
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import paxos._

import scala.concurrent.duration.FiniteDuration

object Demo extends IOApp {

  implicit val longProposalId = new ProposalId[Long] {
    override def next(c: Long): Long = System.currentTimeMillis()
    override def next: Long          = System.currentTimeMillis()
  }

  implicit val nextDelay = FiniteDuration(20, TimeUnit.SECONDS)
  implicit val logger    = Slf4jLogger.getLogger[IO]
  val messenger          = new IOMessenger[Long, String]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _     <- logger.info("Running 9 nodes, three of them propose")
      node1 <- Node[IO, Long, String]("node1", 5, messenger)
      node2 <- Node[IO, Long, String]("node2", 5, messenger)
      node3 <- Node[IO, Long, String]("node3", 5, messenger)
      node4 <- Node[IO, Long, String]("node4", 5, messenger)
      node5 <- Node[IO, Long, String]("node5", 5, messenger)
      node6 <- Node[IO, Long, String]("node6", 5, messenger)
      node7 <- Node[IO, Long, String]("node7", 5, messenger)
      node8 <- Node[IO, Long, String]("node8", 5, messenger)
      node9 <- Node[IO, Long, String]("node9", 5, messenger)

      _ <- Traverse[List]
        .traverse(List(node1, node2, node3, node4, node5, node6, node7, node8, node9))(n => messenger.addNode(n))

      v1 <- node1.propose("cat").start
      v2 <- node2.propose("dog").start
      v3 <- node3.propose("fish").start
      v4 <- node4.listen.start
      v5 <- node5.listen.start
      v6 <- node6.listen.start
      v7 <- node7.listen.start
      v8 <- node8.listen.start
      v9 <- node9.listen.start

      values <- Traverse[List].traverse(List(v1, v2, v3, v4, v5, v6, v7, v8, v9))(_.join)

      _ <- logger.info(s"Accepted values by nodes ${values}")
    } yield ExitCode.Success

}
