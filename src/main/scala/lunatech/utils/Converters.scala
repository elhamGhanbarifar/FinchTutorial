package lunatech.utils

import com.twitter.{util => twitter}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import lunatech.models.Person

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object Converters {

  type UserID = Long

  implicit val encodePerson = new Encoder[Person] {
    override def apply(person: Person): Json = Json.obj(
      "id"   -> person.id.asJson,
      "name" -> person.name.asJson
    )
  }

  implicit val decodePerson: Decoder[Person] = Decoder.instance(c =>
    for {
      name <- c.downField("name").as[String]
    } yield Person(name, Some(System.currentTimeMillis)))

  implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
    case Success(r)  => twitter.Return(r)
    case Failure(ex) => twitter.Throw(ex)
  }

  implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
    case twitter.Return(r) => Success(r)
    case twitter.Throw(ex) => Failure(ex)
  }

  implicit def scalaToTwitterFuture[T](f: Future[T])(
      implicit ec: ExecutionContext): twitter.Future[T] = {
    val promise = twitter.Promise[T]()
    f.onComplete(promise update _)
    promise
  }

  implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
    val promise = Promise[T]()
    f.respond(promise complete _)
    promise.future
  }
}
