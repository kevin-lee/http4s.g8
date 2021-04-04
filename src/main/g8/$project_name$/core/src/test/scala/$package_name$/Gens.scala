package $package_name$

import eu.timepit.refined.numeric.{Interval, Positive}
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string._
import hedgehog._
import $package_name$.core.Data.{IpV4, NonEmptyString, Port}

object Gens {

  def genIpV4: Gen[IpV4] = for {
    ipString <- Gen.int(Range.linear(0, 255)).list(Range.singleton(4)).map(_.mkString("."))
    ip        = refineV[IPv4].unsafeFrom(ipString)
  } yield ip

  def genInvalidIpV4: Gen[String] =
    Gen
      .int(Range.linear(256, Int.MaxValue))
      .list(Range.singleton(4))
      .map(_.mkString("."))

  def genPortNumber: Gen[Port] =
    Gen
      .int(Range.linear(0, 65353))
      .map(port => refineV[Interval.Closed[0, 65353]].unsafeFrom(port))

  def genInvalidPortNumber: Gen[Int] =
    Gen
      .int(Range.linear(65354, Int.MaxValue))

  def genNonEmptyString(genChar: Gen[Char], maxLength: Int Refined Positive): Gen[NonEmptyString] =
    Gen
      .string(genChar, Range.linear(1, maxLength.value))
      .map(s => refineV[NonEmpty].unsafeFrom(s))
}
