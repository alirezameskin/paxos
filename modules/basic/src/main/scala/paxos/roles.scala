package paxos

sealed trait Role
case object ProposerRole extends Role
case object AcceptorRole extends Role
case object LearnerRole extends Role

case class Peer(id: String)
