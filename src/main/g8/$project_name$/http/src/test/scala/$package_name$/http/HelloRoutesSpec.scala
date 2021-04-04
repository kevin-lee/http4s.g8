package $package_name$.http

import cats.effect.IO
import eu.timepit.refined.auto._
import hedgehog._
import hedgehog.runner._
import $package_name$.Gens
import $package_name$.core.Data.Greeting
import $package_name$.service.HelloService
import org.http4s.syntax.all._
import org.http4s.{Method, Request, Status, Uri}

object HelloRoutesSpec extends Properties {
  override def tests: List[Test] = List(
    property("testHelloRoutes", testHelloRoutes)
  )

  def testHelloRoutes: Property = for {
    message <- Gens.genNonEmptyString(Gen.alpha, 50).log("message")
    name    <- Gens.genNonEmptyString(Gen.alpha, 50).log("name")
  } yield {
    val helloService = HelloService[IO](Greeting.Message(message))
    val helloRoutes  = HelloRoutes[IO](helloService)
    val request      = Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"/hello/\${name.value}"))
    val response     = helloRoutes.routes.orNotFound.run(request)
    val actual       = response.unsafeRunSync()
    Result.all(
      List(
        actual.status ==== Status.Ok,
        actual.as[String].unsafeRunSync() ==== s"""{"message":"\${message.value} \${name.value}"}""",
      )
    )
  }
}
