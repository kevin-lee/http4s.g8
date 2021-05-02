package $package_name$.service

import cats.effect.IO
import eu.timepit.refined.auto._
import hedgehog._
import hedgehog.runner._
import $package_name$.Gens
import $package_name$.core.Data.{Greeting, Name}

object GreeterSpec extends Properties {
  override def tests: List[Test] = List(
    property("testGreeter.greet", testGreeterGreet),
  )

  def testGreeterGreet: Property = for {
    messsage <- Gens.genNonEmptyString(Gen.alpha, 50).log("message")
    name     <- Gens.genNonEmptyString(Gen.alpha, 30).log("name")
  } yield {
    val greeter  = Greeter[IO]
    val expected = Greeting(s"\${messsage.value} \${name.value}")

    val result = greeter.greet(Greeting.Message(messsage), Name(name))
    val actual = result.unsafeRunSync()

    expected ==== actual
  }

}
