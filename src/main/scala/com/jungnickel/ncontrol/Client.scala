package com.jungnickel
package ncontrol

import com.ning.http.client
import com.ning.http.client.Response
import com.ning.http.client.cookie.Cookie
import dispatch._
import org.jsoup.Jsoup

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

  private def service = nfon addCookie login

  def callForwardProfiles(): Seq[Profile] = {
    object ProfileList extends (client.Response => List[Profile]) {

      import scala.collection.JavaConverters._

      override def apply(rsp: Response): List[Profile] = {
        val profilePage = Jsoup.parse(rsp.getResponseBodyAsStream, "utf-8", "/")
        val profiles = profilePage
          .select("select[id=profileCallforwards]")
          .select("option").asScala.toList
        profiles map { elem =>
          Profile(elem.`val`().toInt, elem.text())
        }
      }
    }
    import scala.concurrent.duration._

    val req = Http(service / "profiles" / "callforwards" > ProfileList)
    Await.result(req, 10 seconds)
  }

  case class Profile(id: Int, name: String)

}
