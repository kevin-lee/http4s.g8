package $package_name$.core

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.string
import io.estatico.newtype.macros.newtype
import io.circe.{Decoder, Encoder}

object Data {

  type NonEmptyString = String Refined NonEmpty

  type IpV4 = String Refined string.IPv4
  type Port = Int Refined Interval.Closed[0, 65353]

  @newtype case class Greeting(greeting: String)
  object Greeting {
    implicit val encoder: Encoder[Greeting] = deriving
    implicit val decoder: Decoder[Greeting] = deriving
    @newtype case class Message(message: NonEmptyString)
  }
  @newtype case class Name(name: String)

}
