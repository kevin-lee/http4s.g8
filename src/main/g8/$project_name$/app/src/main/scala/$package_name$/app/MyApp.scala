package $package_name$.app

import cats.effect._
import cats.syntax.all._
import $package_name$.config.AppConfig
import $package_name$.http.HttpServer
import org.log4s.{Logger, getLogger}

import java.io.{PrintWriter, StringWriter}
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object MyApp extends IOApp {

  val logger: Logger = getLogger

  implicit val ec: ExecutionContext = scala
    .concurrent
    .ExecutionContext
    .fromExecutor(
      Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() >> 1),
      { th =>
        val stringWriter = new StringWriter()
        val printWriter  = new PrintWriter(stringWriter)
        th.printStackTrace(printWriter)
        logger.error(s"Error in ThreadPoolExecutor: \${stringWriter.toString}")
      },
    )

  override def run(args: List[String]): IO[ExitCode] =
    AppConfig
      .load[IO]()
      .flatMap {
        case Left(err) =>
          IO.raiseError(new RuntimeException(s"Error when reading AppConfig: \${err.prettyPrint(2)}"))

        case Right(config) =>
          HttpServer[IO]
            .stream(
              config,
              ((msg: String) => IO(logger.info(msg))).some,
            )
            .compile[IO, IO, ExitCode]
            .drain
            .as[ExitCode](ExitCode.Success)
      }

}
