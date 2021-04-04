package $package_name$.service

import cats.Applicative
import $package_name$.core.Data._

trait HelloService[F[_]] {
  def hello(name: Name): F[Greeting]
}

object HelloService {

  def apply[F[_]: Applicative](message: Greeting.Message): HelloService[F] = new HelloServiceF[F](message)

  final class HelloServiceF[F[_]: Applicative](message: Greeting.Message) extends HelloService[F] {
    override def hello(name: Name): F[Greeting] =
      Applicative[F].pure(Greeting(s"\${message.message.value} \${name.name}"))
  }

}