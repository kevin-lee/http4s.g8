package $package_name$.service

import cats.Applicative
import $package_name$.core.Data._

trait Greeter[F[_]] {
  def greet(message: Greeting.Message, name: Name): F[Greeting]
}

object Greeter {

  def apply[F[_]: Applicative]: Greeter[F] = new GreeterF[F]

  final class GreeterF[F[_]: Applicative] extends Greeter[F] {
    override def greet(message: Greeting.Message, name: Name): F[Greeting] =
      Applicative[F].pure(Greeting(s"\${message.message.value} \${name.name}"))
  }

}