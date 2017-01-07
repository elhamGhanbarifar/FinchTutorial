package lunatech.models

import lunatech.utils.Converters.UserID
import slick.driver.H2Driver.api._

case class Person(name: String, id: Option[UserID] = None) extends Equals

class Persons(tag: Tag) extends Table[Person](tag, "PERSONS") {
  def id = column[UserID]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def * = (name, id.?) <> (Person.tupled, Person.unapply)
}
