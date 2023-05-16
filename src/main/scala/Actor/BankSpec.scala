package Actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class BankSpec extends TestKit(ActorSystem("BankSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
{

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system) // system is a member of testkit
  }
  import Bank._

  "A BankAccount actor" should {
    import BankAccount._
    val bank = TestActorRef[BankAccount]
    // val bank = system.actorOf(Props[BankAccount])
    "deposit money for an account" in {
      bank ! Deposit("Alice", 500.0)
      expectMsg("Deposited 500.0 into account Alice")
      bank ! GetBalance("Alice")
      expectMsg(500.0)
    }

    "withdraw money from an account" in {
      bank ! Deposit("Josh", 100.0)
      expectMsg("Deposited 100.0 into account Josh")
      bank ! Withdraw("Josh", 50.0)
      expectMsg("Withdrew 50.0 from account Josh")
      bank ! GetBalance("Josh")
      expectMsg(50.0)
    }

    "not allow overdrawn account" in {
      bank ! Deposit("Daniel", 100.0)
      expectMsg("Deposited 100.0 into account Daniel")
      bank ! Withdraw("Daniel", 200.0)
      expectMsg("Insufficient balance in account Daniel")
    }
  }
}

