package lunatech

import java.util.UUID

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import lunatech.controllers.DataSourceController
import lunatech.models.Person
import lunatech.utils.Converters._

object Endpoints {

  val person: Endpoint0      = "person"
  val insert: Endpoint0      = "insert"
  val personsList: Endpoint0 = "persons"

  val datasource: Endpoint[Int] =
    header("datasource").as[Int].mapOutput(context => Ok(context)).handle {
      case e: Error.NotPresent => NotFound(e)
    }

  def readPerson: Endpoint[Person] =
    get(person :: string :: datasource) { (name: String, ctx: Int) =>
      DataSourceController getSingle (name, ctx) map {
        case Some(person) => Ok(person)
        case None         => NotFound(new Exception(s"Could not find $name"))
      }
    }.handle {
      case e: Error.RequestErrors => BadRequest(e)
      case e: Error.NotPresent    => BadRequest(e)
      case e: Error.NotParsed     => BadRequest(e)
      case e: Error.NotValid      => BadRequest(e)
      case e: Error               => BadRequest(e)
    }

  def getPersons: Endpoint[Seq[Person]] =
    get(personsList :: string :: datasource) { (name: String, ctx: Int) =>
      DataSourceController getList (name, ctx) map {
        case Some(person) => Ok(person)
        case None         => NotFound(new Exception(s"Could not find $name"))
      }
    }.handle {
      case e: Error.RequestErrors => BadRequest(e)
      case e: Error.NotPresent    => BadRequest(e)
      case e: Error.NotParsed     => BadRequest(e)
      case e: Error.NotValid      => BadRequest(e)
      case e: Error               => BadRequest(e)
    }

  val postedPerson: Endpoint[Person] =
    body.as[Person].map(p => Person(p.name, Some(System.currentTimeMillis())))
  def insertPerson: Endpoint[Person] =
    post(insert :: postedPerson :: datasource) { (person: Person, ctx: Int) =>
      DataSourceController insertSingle (person, ctx) map {
        case Some(person) => Created(person)
        case None         => InternalServerError(new Exception(s"Could not insert $person"))
      }
    }.handle {
      case e: Error.RequestErrors => BadRequest(e)
      case e: Error.NotPresent    => BadRequest(e)
      case e: Error.NotParsed     => BadRequest(e)
      case e: Error.NotValid      => BadRequest(e)
      case e: Error               => BadRequest(e)
    }

  def readEndpoints(): Service[Request, Response] = {
    readPerson :+: getPersons
  }.handle({
      case e: Exception => { e.printStackTrace(); InternalServerError(e) }
    })
    .toServiceAs[Application.Json]

  def writeEndpoints(): Service[Request, Response] = {
    insertPerson
  }.handle({
      case e: Exception => { e.printStackTrace(); InternalServerError(e) }
    })
    .toServiceAs[Application.Json]

}
