package Actor

import java.io.{File, PrintWriter}
import akka.actor.{Actor, ActorLogging, ActorSystem,Props}

object LogFile extends App{

  case class LogWarn(message: String)
  case class LogInfo(message: String)
  case class RenameFile(newName: String)

  class LoggerActor(warnFile: File, infoFile: File) extends Actor with ActorLogging {

    override def receive: Receive = withWriters(None, None)

    def withWriters(maybeWarnWriter: Option[PrintWriter], maybeInfoWriter: Option[PrintWriter]): Receive = {

      case LogWarn(message) =>
        val newWarnWriter = maybeWarnWriter.getOrElse(newWriter(warnFile))
        newWarnWriter.println(message)
        newWarnWriter.flush()
        context.become(withWriters(Some(newWarnWriter), maybeInfoWriter))

      case LogInfo(message) =>
        val newInfoWriter = maybeInfoWriter.getOrElse(newWriter(infoFile))
        newInfoWriter.println(message)
        newInfoWriter.flush()
        context.become(withWriters(maybeWarnWriter, Some(newInfoWriter)))

      case RenameFile(newName) =>
        maybeWarnWriter.foreach(_.close())
        maybeInfoWriter.foreach(_.close())
        warnFile.renameTo(new File(newName + "_warn.log"))
        infoFile.renameTo(new File(newName + "_info.log"))
        val newWarnWriter = newWriter(warnFile)
        val newInfoWriter = newWriter(infoFile)
        context.become(withWriters(Some(newWarnWriter), Some(newInfoWriter)))
    }

    def newWriter(file: File): PrintWriter = {
      new PrintWriter(file)
    }
  }

  val system = ActorSystem("LogFile")
  val warnFile = new File("warn.log")
  val infoFile = new File("info.log")
  val loggerActor = system.actorOf(Props(classOf[LoggerActor], warnFile, infoFile), "loggerActor")

  loggerActor ! LogWarn("This is a warning message")
  loggerActor ! LogInfo("This is an info message")

  loggerActor ! RenameFile("log")

  loggerActor ! LogWarn("This is a new warning message")
  loggerActor ! LogInfo("This is a new info message")

  system.terminate()
}
