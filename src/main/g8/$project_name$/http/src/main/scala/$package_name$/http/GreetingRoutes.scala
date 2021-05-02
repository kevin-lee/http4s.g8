package $package_name$.http

import cats.effect.Sync
import cats.syntax.all._
import eu.timepit.refined.auto._
import io.circe.Json
import io.circe.syntax._
import $package_name$.core.Data.{Name, Greeting}
import $package_name$.service.Greeter
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait GreetingRoutes[F[_]] {
  def routes: HttpRoutes[F]
}

object GreetingRoutes {
  def apply[F[_]: Sync](
    message: Greeting.Message, greeter: Greeter[F]
  ): GreetingRoutes[F] =
    new GreetingRoutesF[F](message, greeter)

  final class GreetingRoutesF[F[_]: Sync](
    message: Greeting.Message,
    greeter: Greeter[F]
  ) extends GreetingRoutes[F]
       with Http4sDsl[F] {

    override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "greet" / name =>
        for {
          greeting <- greeter.greet(message, Name(name))
          result    = Json.obj(
            "message" -> greeting.asJson
          )
          response <- Ok(result)
        } yield response

    }

  }
}
