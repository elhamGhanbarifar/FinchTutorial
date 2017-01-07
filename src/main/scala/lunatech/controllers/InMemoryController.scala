package lunatech.controllers
import com.twitter.util.Future
import lunatech.models.Person
import lunatech.utils.Converters.UserID

import scala.collection.mutable

object InMemoryController extends DataSource {

  private[this] val db: mutable.Map[UserID, Person] = mutable.Map[UserID, Person](
    171108361L -> Person("Inaki", Some(171108361L)),
    171108388L -> Person("John Doe", Some(171108388L))
  )

  override def getPerson(name: String): Future[Option[Person]] =
    Future.value({
      db.filter(p => p._2.name.equalsIgnoreCase(name)).toList match {
        case y :: ys => Some(y._2)
        case _       => None
      }
    })

  override def getPersons(name: String): Future[Option[Seq[Person]]] =
    Future.value(Option(db.filter(p => p._2.name.equalsIgnoreCase(name)).map(_._2).toSeq))

  override def createPerson(person: Person): Future[Option[Person]] = Future.value {
    db.put(person.id.get, person); Some(person)
  }

}
