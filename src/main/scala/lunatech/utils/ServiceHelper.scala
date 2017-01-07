package lunatech.utils

import cats.data.NonEmptyList
import com.twitter.finagle.Service
import com.twitter.finagle.http.Response.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.service.ConstantService
import com.twitter.util.{Duration, Future}

object ServiceHelper {

  def createNormalService(content: String, rep: Response = new Ok): Service[Request, Response] = {
    rep.write(content)
    val response: Future[Response] = Future(rep)
    new ConstantService[Request, Response](response)
  }

  def slowService(sleepMillis: Long = 2000) = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = Future.value {
      Thread.sleep(sleepMillis)
      Response()
    }
  }

  def closableService() = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      this.close(Duration.fromMilliseconds(0)).map[Response](_ => Response())
    }
  }

  def compose(service1: Service[Request, Response],
              service2: Service[Request, Response]): Service[Request, Response] =
    Service.mk { (request1: Request) =>
      service1(request1).flatMap(rr =>
        if (rr.status == Status.NotFound) service2(request1) else Future(rr))
    }

  def composeN(services: NonEmptyList[Service[Request, Response]]): Service[Request, Response] =
    services.reduceLeft(compose(_, _))

}
