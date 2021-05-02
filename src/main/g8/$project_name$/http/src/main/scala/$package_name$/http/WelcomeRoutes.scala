package $package_name$.http

import cats.effect.Sync
import cats.syntax.all._
import eu.timepit.refined.auto._
import io.circe.Json
import $package_name$.core.Data.{Name, Welcome}
import $package_name$.service.Welcomer
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait WelcomeRoutes[F[_]] {
  def routes: HttpRoutes[F]
}

object WelcomeRoutes {
  def apply[F[_]: Sync](welcomer: Welcomer[F]): WelcomeRoutes[F] =
    new WelcomeRoutesF[F](welcomer)

  final class WelcomeRoutesF[F[_]: Sync](welcomer: Welcomer[F]) extends WelcomeRoutes[F]
       with Http4sDsl[F] {

    override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "welcome" / name =>
        for {
          welcomeMsg <- welcomer.welcome(Name(name))
          result      = Json.obj(
              "message" -> Json.fromString(Welcome.render(welcomeMsg))
            )
          response <- Ok(result)
        } yield response

    }

  }
}
