package $package_name$.http

import cats.effect.IO
import eu.timepit.refined.auto._
import hedgehog._
import hedgehog.runner._
import $package_name$.core.Data._
import $package_name$.Gens
import $package_name$.service.Welcomer
import org.http4s.syntax.all._
import org.http4s.{Method, Request, Status, Uri}

object WelcomeRoutesSpec extends Properties {

  override def tests: List[Test] = List(
    property("testWelcomeRoutes.GET.welcome", testWelcomeRoutesGetWelcome),
  )

  def testWelcomeRoutesGetWelcome: Property = for {
    where <- Gens.genNonEmptyString(Gen.alpha, 50).log("where")
    name  <- Gens.genNonEmptyString(Gen.alpha, 50).log("name")
  } yield {
    val welcomer      = Welcomer[IO](Where(where))
    val welcomeRoutes = WelcomeRoutes[IO](welcomer)
    val request       = Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"/welcome/\${name.value}"))
    val response      = welcomeRoutes.routes.orNotFound.run(request)
    val actual        = response.unsafeRunSync()
    Result.all(
      List(
        actual.status ==== Status.Ok,
        actual.as[String].unsafeRunSync() ==== s"""{"message":"\${name.value}, welcome to \${where.value}"}""",
      )
    )
  }

}
