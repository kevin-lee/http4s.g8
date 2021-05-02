package $package_name$.config

import eu.timepit.refined.auto._
import hedgehog._
import hedgehog.runner._
import $package_name$.Gens
import $package_name$.config.AppConfig.{GreetingConfig, ServerConfig, WelcomeConfig}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object AppConfigSpec extends Properties {
  override def tests: List[Test] = List(
    property("testServerConfig", testServerConfig),
    property("testInvalidServerConfig", testInvalidServerConfig),
    property("testWelcomeConfig", testWelcomeConfig),
    example("testInvalidWelcomeConfig", testInvalidWelcomeConfig),
    property("testGreetingConfig", testGreetingConfig),
    example("testInvalidGreetingConfig", testInvalidGreetingConfig),
    property("testAppConfig", testAppConfig),
    property("testInvalidAppConfig", testInvalidAppConfig),
  )

  def testServerConfig: Property = for {
    ipString <- Gens.genIpV4.log("ipString")
    portNum  <- Gens.genPortNumber.log("portNum")
  } yield {
    final case class ExpectedConfig(
      server: ServerConfig
    )
    val expected = ExpectedConfig(
      ServerConfig(ServerConfig.HostAddress(ipString), ServerConfig.PortNumber(portNum))
    )
    val config   = ConfigSource.string(
      s"""
         |server {
         |  host-address: "\${ipString.value}"
         |  port-number: \${portNum.value}
         |}
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        actual ==== expected
      case Left(failure) =>
        Result.failure.log(s"\${failure.prettyPrint(2)}")
    }
  }

  def testInvalidServerConfig: Property = for {
    ipString <- Gens.genInvalidIpV4.log("ipString")
    portNum  <- Gens.genInvalidPortNumber.log("portNum")
  } yield {
    final case class ExpectedConfig(
      server: ServerConfig
    )
    val config = ConfigSource.string(
      s"""
         |server {
         |  host-address: "\$ipString"
         |  port-number: \$portNum
         |}
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        Result.failure.log(s"Expected config parse failure but got \${actual.toString}")

      case Left(failure) =>
        val failureMessage = failure.prettyPrint(2)
        Result.all(
          List(
            Result
              .assert(failureMessage.contains("server.host-address"))
              .log("Expected failure at server.host-address but didn't find"),
            Result
              .assert(failureMessage.contains("server.port-number"))
              .log("Expected failure at server.port-number but didn't find"),
          )
        )
    }
  }

  def testWelcomeConfig: Property = for {
    to <- Gens.genNonEmptyString(Gen.alpha, 20).log("to")
  } yield {
    final case class ExpectedConfig(
      welcome: WelcomeConfig
    )
    val expected = ExpectedConfig(WelcomeConfig(WelcomeConfig.Where(to)))
    val config   = ConfigSource.string(
      s"""
         |welcome.to: "\${to.value}"
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        actual ==== expected
      case Left(failure) =>
        Result.failure.log(s"\${failure.prettyPrint(2)}")
    }
  }

  def testInvalidWelcomeConfig: Result = {
    final case class ExpectedConfig(
      welcome: WelcomeConfig
    )
    val config = ConfigSource.string(
      s"""
         |welcome.to: ""
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        Result.failure.log(s"Expected config parse failure but got \${actual.toString}")

      case Left(failure) =>
        val failureMessage = failure.prettyPrint(2)
        Result.all(
          List(
            Result
              .assert(failureMessage.contains("welcome.to"))
              .log("Expected failure at welcome.to but didn't find"),
          )
        )
    }
  }

  def testGreetingConfig: Property = for {
    message <- Gens.genNonEmptyString(Gen.alpha, 20).log("message")
  } yield {
    final case class ExpectedConfig(
      greeting: GreetingConfig
    )
    val expected = ExpectedConfig(GreetingConfig(GreetingConfig.Message(message)))
    val config   = ConfigSource.string(
      s"""
         |greeting {
         |  message: "\${message.value}"
         |}
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        actual ==== expected
      case Left(failure) =>
        Result.failure.log(s"\${failure.prettyPrint(2)}")
    }
  }

  def testInvalidGreetingConfig: Result = {
    final case class ExpectedConfig(
      greeting: GreetingConfig
    )
    val config   = ConfigSource.string(
      s"""
         |greeting {
         |  message: ""
         |}
         |""".stripMargin
    )
    config.load[ExpectedConfig] match {
      case Right(actual) =>
        Result.failure.log(s"Expected config parse failure but got \${actual.toString}")

      case Left(failure) =>
        val failureMessage = failure.prettyPrint(2)
        Result.all(
          List(
            Result
              .assert(failureMessage.contains("greeting.message"))
              .log("Expected failure at greeting.message but didn't find"),
          )
        )
    }
  }

  def testAppConfig: Property = for {
    ipString <- Gens.genIpV4.log("ipString")
    portNum  <- Gens.genPortNumber.log("portNum")
    greetingMessage  <- Gens.genNonEmptyString(Gen.alpha, 20).log("greetingMessage")
    to  <- Gens.genNonEmptyString(Gen.alpha, 20).log("to")
  } yield {
    val expected = AppConfig(
      ServerConfig(ServerConfig.HostAddress(ipString), ServerConfig.PortNumber(portNum)),
      GreetingConfig(GreetingConfig.Message(greetingMessage)),
      WelcomeConfig(WelcomeConfig.Where(to)),
    )
    val config   = ConfigSource.string(
      s"""server {
         |  host-address: "\${ipString.value}"
         |  port-number: \${portNum.value}
         |}
         |greeting {
         |  message: "\${greetingMessage.value}"
         |}
         |welcome.to: "\${to.value}"
         |""".stripMargin
    )
    config.load[AppConfig] match {
      case Right(actual) =>
        actual ==== expected
      case Left(failure) =>
        Result.failure.log(s"\${failure.prettyPrint(2)}")
    }
  }

  def testInvalidAppConfig: Property = for {
    ipString <- Gens.genInvalidIpV4.log("ipString")
    portNum  <- Gens.genInvalidPortNumber.log("portNum")
  } yield {
    val config   = ConfigSource.string(
      s"""server {
         |  host-address: "\$ipString"
         |  port-number: \$portNum
         |}
         |greeting {
         |  message: ""
         |}
         |welcome.to: ""
         |""".stripMargin
    )
    config.load[AppConfig] match {
      case Right(actual) =>
        Result.failure.log(s"Expected config parse failure but got \${actual.toString}")

      case Left(failure) =>
        val failureMessage = failure.prettyPrint(2)
        Result.all(
          List(
            Result
              .assert(failureMessage.contains("server.host-address"))
              .log("Expected failure at server.host-address but didn't find"),
            Result
              .assert(failureMessage.contains("server.port-number"))
              .log("Expected failure at server.port-number but didn't find"),
            Result
              .assert(failureMessage.contains("greeting.message"))
              .log("Expected failure at greeting.message but didn't find"),
            Result
              .assert(failureMessage.contains("welcome.to"))
              .log("Expected failure at welcome.to but didn't find"),
          )
        )
    }
  }

}
