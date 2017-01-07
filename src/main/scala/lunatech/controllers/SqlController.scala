package lunatech.controllers

import com.twitter.util.{Future => TwitterFuture}
import lunatech.models.{Person, Persons}
import lunatech.utils.Converters
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object SqlController extends DataSource {

  val persons: TableQuery[Persons]                         = TableQuery[Persons]
  val db: _root_.slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("h2mem1")

  private val insertPerson = persons returning persons.map(_.id) into ((item, id) =>
                                                                         Option(
                                                                           item.copy(
                                                                             id = Some(id))))

  val createDatabase: Future[Unit] = db.run(
    DBIO.seq(
      persons.schema.create,
      persons += Person("Inaki"),
      persons += Person("John Doe")
    ))

  private def prepareByNameQuery(name: String) = persons.filter(_.name === name)

  override def getPerson(name: String): TwitterFuture[Option[Person]] = {
    val result: Future[Option[Person]] = db.run(prepareByNameQuery(name).take(1).result.map {
      case y +: Seq() => Some(y)
      case _          => None
    })
    Converters.scalaToTwitterFuture(result)
  }

  override def getPersons(name: String): TwitterFuture[Option[Seq[Person]]] = {
    val result: Future[Option[Seq[Person]]] =
      db.run(prepareByNameQuery(name).result.map(Option(_)))
    Converters.scalaToTwitterFuture(result)
  }

  override def createPerson(person: Person): TwitterFuture[Option[Person]] =
    Converters.scalaToTwitterFuture(db.run(insertPerson += person))

}
