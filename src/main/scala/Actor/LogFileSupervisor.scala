package Actor

//import the necessary utilities
import java.io.File
import akka.actor.{Actor, ActorInitializationException, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.actor.SupervisorStrategy.{Restart, Stop}

object LogFileSupervisor extends App {

  import LogFile._

 // supervisor actor
  case class LoggerSupervisor(loggerProps: Props) extends Actor with ActorLogging {
    private val loggerActor: ActorRef = context.actorOf(loggerProps, "loggerActor")

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: ActorInitializationException => Restart // actor fails to initialize than this exception will throw
      case _: Exception => Stop // for other type of exception
    }

    override def receive: Receive = {
      case msg => loggerActor.forward(msg) // the child actor loggerActor forwarded the message to the original sender
           //The forward method sends the message to another actor and preserves the original sender information
          // any reply will be sent back to the original sender
    }
  }

  val system: ActorSystem = ActorSystem("LogFile")
  val warnFile: File = new File("warn.log")
  val infoFile: File = new File("info.log")
  val loggerProps: Props = Props(classOf[LoggerActor], warnFile, infoFile)
  val loggerSupervisor: ActorRef = system.actorOf(Props(LoggerSupervisor(loggerProps)), "loggerSupervisor")

  loggerSupervisor ! LogWarn("This is a Supervisor warning message")
  loggerSupervisor ! LogInfo("This is an Supervisor info message")
  system.terminate()

}
