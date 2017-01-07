package lunatech.filters

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

case class HeaderValidator(header: String) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val headerAvailable = request.headerMap.exists(_._1 == header)
    println(s"Is the $header header set? $headerAvailable")
    service(request)
  }
}

object HeaderValidator {}
