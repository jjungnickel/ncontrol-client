package com.jungnickel
package ncontrol

import com.ning.http.client
import com.ning.http.client.Response
import com.ning.http.client.cookie.Cookie
import dispatch._

import scala.concurrent.Await

object Client {
  def apply(customer: String, extension: Int, pin: Int) = new Client(customer, extension, pin)
}

class Client(customer: String, extension: Int, pin: Int) {
  /**
   * Performs a login using the given credentials and upon successful authentication
   * returns a cookie that can be used by subsequent requests to act as the given extension.
   */
  lazy val login: Cookie = {
    // todo: should be a future, so we don't run a request when we don't need to
    object LoginCookie extends (client.Response => Cookie) {

      import scala.collection.JavaConverters._

      override def apply(rsp: Response): Cookie = {
        val cookies = rsp.getCookies.asScala
        cookies.filter(_.getName == "userportal_perm").head
      }
    }
    val req = nfon / "login" << Map(
      "customerNumber" -> customer,
      "extensionNumber" -> extension.toString,
      "password" -> pin.toString,
      "permanentLogin" -> "permanentLogin",
      "login" -> "",
      "form_submit_loginForm" -> "loginForm"
    )

    import scala.concurrent.duration._

    Await.result(Http(req > LoginCookie), 10 seconds)
  }
  implicit val executor = Defaults.executor

  private val nfon = host("ncontrol.nfon.net").secure / "de" setHeader("Accept-Language", "de")

  def service() = nfon addCookie login

  case class Profile(id: Int, name: String)

}
