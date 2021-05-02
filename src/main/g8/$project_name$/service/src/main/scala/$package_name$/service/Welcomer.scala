package $package_name$.service

import cats.Applicative
import $package_name$.core.Data._

trait Welcomer[F[_]] {
  def welcome(name: Name): F[Welcome]
}

object Welcomer {

  def apply[F[_]: Applicative](where: Where): Welcomer[F] = new WelcomerF[F](where)

  final class WelcomerF[F[_]: Applicative](where: Where) extends Welcomer[F] {
    override def welcome(name: Name): F[Welcome] =
      Applicative[F].pure(Welcome(name, where))
  }

}