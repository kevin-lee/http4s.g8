package $package_name$.http

import syntax.all._
import cats.syntax.all._
import cats.effect._
import fs2.Stream
import $package_name$.core.Data._
import $package_name$.config.AppConfig
import $package_name$.service.{Greeter, Welcomer}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.syntax.all._

import scala.concurrent.ExecutionContext

trait HttpServer[F[_]] {
  def stream(
    appConfig: AppConfig,
    logAction: Option[String => F[Unit]],
  )(implicit executionContext: ExecutionContext): Stream[F, ExitCode]
}

object HttpServer {
  def apply[F[_]: ConcurrentEffect: Timer]: HttpServer[F] =
    new HttpServerF[F]

  final class HttpServerF[F[_]: ConcurrentEffect: Timer] extends HttpServer[F] {

    override def stream(
      appConfig: AppConfig,
      logAction: Option[String => F[Unit]],
    )(
      implicit executionContext: ExecutionContext
    ): Stream[F, ExitCode] = {
      for {
        greeter  <- streamPureF(Greeter[F])
        welcomer <- streamPureF(Welcomer[F](Where(appConfig.welcome.to.where)))
        routes   <- streamPureF(
            (
              GreetingRoutes[F](
                Greeting.Message(appConfig.greeting.message.message),
                greeter
              ).routes <+> WelcomeRoutes[F](
                welcomer
              ).routes
            ).orNotFound
          )
        httpApp  =
          Logger.httpApp(
            logHeaders = true,
            logBody = true,
            logAction = logAction,
          )(
            routes
          )
        exitCode <-
          BlazeServerBuilder[F](executionContext)
            .bindHttp(
              appConfig.server.portNumber.portNumber.value,
              appConfig.server.hostAddress.hostAddress.value,
            )
            .withHttpApp(httpApp)
            .serve
      } yield exitCode
    }
  }
}
