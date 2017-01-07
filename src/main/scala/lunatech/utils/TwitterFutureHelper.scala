package lunatech.utils

import cats.Monad
import com.twitter.util.Future

object TwitterFutureHelper {
  implicit object FutureMonad extends Monad[Future] {
    override def map[A, B](fa: Future[A])(f: (A) => B): Future[B]             = fa.map(f)
    override def flatMap[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa.flatMap(f)

    /**
      * Note that while this implementation will not compile with `@tailrec`,
      * it is in fact stack-safe, corresponding to the Cats documentation.
      */
    override def tailRecM[A, B](a: A)(f: (A) => Future[Either[A, B]]): Future[B] = {
      f(a).flatMap {
        case Left(b1) => tailRecM(b1)(f)
        case Right(b) => Future(b)
      }
    }

    override def pure[A](x: A): Future[A] = Future(x)
  }

}
