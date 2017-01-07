package lunatech.controllers

import com.twitter.util.Future
import lunatech.models.Person

trait DataService {
  val repo: DataSource
  def getPerson(name: String): Future[Option[Person]]
  def getPersons(name: String): Future[Option[Seq[Person]]]
  def insertPerson(person: Person): Future[Option[Person]]
}

object SqlService extends DataService {
  val repo: DataSource                                               = SqlController
  override def getPerson(name: String): Future[Option[Person]]       = repo.getPerson(name)
  override def getPersons(name: String): Future[Option[Seq[Person]]] = repo.getPersons(name)

  override def insertPerson(person: Person): Future[Option[Person]] = repo.createPerson(person)
}

object InMemoryService extends DataService {
  val repo: DataSource                                               = InMemoryController
  override def getPerson(name: String): Future[Option[Person]]       = repo.getPerson(name)
  override def getPersons(name: String): Future[Option[Seq[Person]]] = repo.getPersons(name)

  override def insertPerson(person: Person): Future[Option[Person]] = repo.createPerson(person)
}

object MixedService extends DataService {
  val repo: DataSource                                               = MixedController
  override def getPerson(name: String): Future[Option[Person]]       = repo.getPerson(name)
  override def getPersons(name: String): Future[Option[Seq[Person]]] = repo.getPersons(name)

  override def insertPerson(person: Person): Future[Option[Person]] = ???
}

object DataSourceServices {
  def ctxToService[A, B](ctx: Int, f: (DataService) => A => Future[B], empty: A => Future[B]) =
    ctx.toString.substring(0, 1) match {
      case "1" => f(SqlService)
      case "2" => f(InMemoryService)
      case "3" => f(MixedService)
      case _   => empty
    }

  def ctxToServiceSeq[A, B](ctx: Int,
                            f: (DataService) => A => Future[Option[Seq[B]]],
                            empty: A => Future[Option[Seq[B]]]) =
    ctx.toString.substring(0, 1) match {
      case "1" => f(SqlService)
      case "2" => f(InMemoryService)
      case "3" => f(MixedService)
      case _   => empty
    }

  def empty[A, B](a: A)(in: B) = Future.value(a)

}

object DataSourceController {
  import DataSourceServices._

  private val getPerson    = (ds: DataService) => ds.getPerson _
  private val getPersons   = (ds: DataService) => ds.getPersons _
  private val insertPerson = (ds: DataService) => ds.insertPerson _

  def getSingle(name: String, context: Int): Future[Option[Person]] =
    ctxToService(context, getPerson, empty(None))(name)

  def getList(name: String, context: Int): Future[Option[Seq[Person]]] =
    ctxToServiceSeq(context, getPersons, empty(Option(Seq())))(name)

  def insertSingle(person: Person, context: Int): Future[Option[Person]] =
    ctxToService(context, insertPerson, empty(None))(person)

}
