package lunatech.controllers

import com.twitter.util.Future
import lunatech.models.Person

trait DataSource {

  def getPerson(name: String): Future[Option[Person]]
  def getPersons(name: String): Future[Option[Seq[Person]]]

  def createPerson(person: Person): Future[Option[Person]]

}
