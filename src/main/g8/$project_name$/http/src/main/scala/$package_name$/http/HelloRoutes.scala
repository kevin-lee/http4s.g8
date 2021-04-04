package $package_name$.http

import cats.effect.Sync
import cats.syntax.all._
import io.circe.Json
import $package_name$.core.Data.Name
import $package_name$.service.HelloService
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

trait HelloRoutes[F[_]] {
  def routes: HttpRoutes[F]
}

object HelloRoutes {
  def apply[F[_]: Sync](helloService: HelloService[F]): HelloRoutes[F] =
    new HelloRoutesF[F](helloService)

  final class HelloRoutesF[F[_]: Sync](helloService: HelloService[F]) extends HelloRoutes[F] with Http4sDsl[F] {

    override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- helloService.hello(Name(name))
          result    = Json.obj(
                        "message" -> Json.fromString(greeting.greeting)
                      )
          response <- Ok(result)
        } yield response

    }

  }
}
