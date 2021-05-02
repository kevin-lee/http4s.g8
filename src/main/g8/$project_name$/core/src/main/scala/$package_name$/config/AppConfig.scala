package $package_name$.config

import cats._
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.auto._
import io.estatico.newtype.macros.newtype
import $package_name$.core.Data.{IpV4, NonEmptyString, Port}
import pureconfig.ConfigReader.Result
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto._

final case class AppConfig(
  server: AppConfig.ServerConfig,
  greeting: AppConfig.GreetingConfig,
  welcome: AppConfig.WelcomeConfig,
)

object AppConfig {

  final case class ServerConfig(
    hostAddress: ServerConfig.HostAddress,
    portNumber: ServerConfig.PortNumber,
  )
  object ServerConfig {

    @newtype case class HostAddress(hostAddress: IpV4)
    object HostAddress {
      implicit val configReader: ConfigReader[HostAddress] = deriving
    }

    @newtype case class PortNumber(portNumber: Port)
    object PortNumber {
      implicit val configReader: ConfigReader[PortNumber] = deriving
    }
  }

  final case class GreetingConfig(message: GreetingConfig.Message)
  object GreetingConfig {
    @newtype case class Message(message: NonEmptyString)
    object Message {
      implicit val configReader: ConfigReader[Message] = deriving
    }
  }

  final case class WelcomeConfig(message: WelcomeConfig.Message)
  object WelcomeConfig {
    @newtype case class Message(message: NonEmptyString)
    object Message {
      implicit val configReader: ConfigReader[Message] = deriving
    }
  }

  def load[F[_]: Applicative](): F[Result[AppConfig]] =
    Applicative[F].pure(ConfigSource.defaultApplication.load[AppConfig])

}
