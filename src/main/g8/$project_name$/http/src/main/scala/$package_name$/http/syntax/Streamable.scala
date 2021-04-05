package $package_name$.http.syntax

import cats.Applicative
import cats.effect._
import fs2.Stream

trait Streamable {

  def streamF[F[_]: Sync, A](a: => A): Stream[F, A] =
    Stream.eval(Sync[F].delay(a))

  def streamPureF[F[_]: Applicative, A](a: A): Stream[F, A] =
    Stream.eval(Applicative[F].pure(a))

}
