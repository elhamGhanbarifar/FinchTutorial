package lunatech.controllers
import cats.data.OptionT
import com.twitter.util.Future
import lunatech.models.Person
import lunatech.utils.TwitterFutureHelper._

object MixedController extends DataSource {
  override def getPerson(name: String): Future[Option[Person]] = ???

  override def getPersons(name: String): Future[Option[Seq[Person]]] = {
    val sqlPersons: Future[Option[Seq[Person]]]      = SqlController getPersons name
    val inMemoryPersons: Future[Option[Seq[Person]]] = InMemoryController getPersons name
    for {
      sqlPerson      <- OptionT(sqlPersons)
      inMemoryPerson <- OptionT(inMemoryPersons)
    } yield sqlPerson.toList ::: inMemoryPerson.toList
  }.value

  override def createPerson(person: Person): Future[Option[Person]] = ???
}
