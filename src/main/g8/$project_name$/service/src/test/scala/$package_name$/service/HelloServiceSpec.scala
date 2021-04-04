package $package_name$.service

import cats.effect.IO
import eu.timepit.refined.auto._
import hedgehog._
import hedgehog.runner._
import $package_name$.Gens
import $package_name$.core.Data.{Greeting, Name}

object HelloServiceSpec extends Properties {
  override def tests: List[Test] = List(
    property("testHelloService.hello", testHelloServiceHello)
  )

  def testHelloServiceHello: Property = for {
    message <- Gens.genNonEmptyString(Gen.alpha, 30).log("message")
    name    <- Gens.genNonEmptyString(Gen.alpha, 30).log("name")
  } yield {
    val helloService = HelloService[IO](Greeting.Message(message))
    val expected     = Greeting(s"\${message.value} \${name.value}")

    val result = helloService.hello(Name(name))
    val actual = result.unsafeRunSync()

    expected ==== actual
  }
}
