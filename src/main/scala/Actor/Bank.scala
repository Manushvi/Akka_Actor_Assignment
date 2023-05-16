package Actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Bank extends App {
  val system = ActorSystem("Bank")

  object BankAccount {
    case class Deposit(account: String, amount: Double)
    case class Withdraw(account: String, amount: Double)
    case class GetBalance(account: String)
  }

  class BankAccount extends Actor {
    import BankAccount._

    override def receive: Receive = withBalances(Map.empty)

    def withBalances(balances: Map[String, Double]): Receive = {
      case Deposit(account, amount) =>
        val updatedBalances = balances + (account -> (balances.getOrElse(account, 0.0) + amount))
        sender() ! s"Deposited $amount into account $account"
        context.become(withBalances(updatedBalances))

      case Withdraw(account, amount) =>
        val balance = balances.getOrElse(account, 0.0)
        if (balance >= amount && amount >= 0.0) {
          val updatedBalances = balances + (account -> (balance - amount))
          sender() ! s"Withdrew $amount from account $account"
          context.become(withBalances(updatedBalances))
        } else if (amount < 0.0) {
          sender() ! "Amount is not correctly defined"
        } else {
          sender() ! s"Insufficient balance in account $account"
        }

      case GetBalance(account) =>
        sender() ! balances.getOrElse(account, 0.0)
    }
  }

  object Person {
    case class LiveALife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAccount._

    def receive: Receive = {
      case LiveALife(account) =>
        account ! Deposit("123-456-789", 10000.0)
        account ! Withdraw("123-456-789", 90000.0)
        account ! Withdraw("123-456-789", 500.0)
        account ! GetBalance("123-456-789")
        account ! Withdraw("122-345", 500.0)

      case message => println(message.toString)
    }
  }

  import Person._
  val account = system.actorOf(Props[BankAccount], "bankAccount")
  val person = system.actorOf(Props[Person], "billionaire")
  person ! LiveALife(account)

}