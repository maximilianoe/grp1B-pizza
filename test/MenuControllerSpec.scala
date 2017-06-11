/**
  * Created by Hasib on 04.06.2017.
  */

import controllers.MenuController
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.Helpers.{GET, OK, POST, SEE_OTHER, contentAsString, defaultAwaitTimeout, redirectLocation, running, status}
import play.api.test.{FakeApplication, FakeRequest}

@RunWith(classOf[JUnitRunner])
class MenuControllerSpec extends Specification {

  def memDB[T](code: => T) =
    running(FakeApplication(additionalConfiguration = Map(
      "db.default.driver" -> "org.postgresql.Driver",
      "db.default.url" -> "jdbc:h2:mem:test;MODE=PostgreSQL"
    )))(code)

  "MenuController" should {

    "add a new Product to Menu" in memDB {
      val request = FakeRequest(POST, "/addToMenu").withFormUrlEncodedBody(
        "Produktname" -> "Fungi",
        "Preis je Einheit" -> "0.24",
        "Kategorie" -> "Pizza"
      )
      val result = MenuController.addToMenu()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editMenu")
    }

    "add existing Product to Menu" in memDB {
      val request = FakeRequest(POST, "/addToMenu").withFormUrlEncodedBody(
        "Produktname" -> "Margarita",
        "Preis je Einheit" -> "0.23",
        "Kategorie" -> "Pizza"
      )
      val result = MenuController.addToMenu()(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/attemptFailed?errorcode=productDoesExist")
    }

    "add a new Category to Menu" in memDB {
      val request = FakeRequest(POST, "/addCategory").withFormUrlEncodedBody(
        "Name" -> "Spirituosen",
        "Maßeinheit" -> "Stk"
      )
      val result = MenuController.addCategory(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editCategory")
    }

    "add existing Category to Menu" in memDB {
      val request = FakeRequest(POST, "/addCategory").withFormUrlEncodedBody(
        "Name" -> "Pizza",
        "Maßeinheit" -> "cm"
      )
      val result = MenuController.addCategory(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/attemptFailed?errorcode=categoryused")
    }

    "alter a Product in Menu" in memDB {
      val request = FakeRequest(POST, "/updateInMenu").withFormUrlEncodedBody(
        "Id" -> "101",
        "Neuer Name" -> "Rucola",
        "Neuer Preis" -> "0.25",
        "Aktivieren" -> "true"
      )
      val result = MenuController.updateInMenu(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editMenu")
    }

    "alter a Category in Menu" in memDB {
      val request = FakeRequest(POST, "/updateCategory").withFormUrlEncodedBody(
        "Alter Name" -> "Pizza",
        "Neuer Name" -> "FingerFood"
      )
      val result = MenuController.updateCategory(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editCategory")
    }

    "remove a Product From Menu" in memDB {
      val request = FakeRequest(POST, "/rmFromMenu").withFormUrlEncodedBody(
        "Id" -> "101"
      )
      val result = MenuController.rmFromMenu(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editMenu")
    }

    "remove a Category From Menu" in memDB {
      val request = FakeRequest(POST, "/rmCategory").withFormUrlEncodedBody(
        "Kategorie" -> "Getränk"
      )
      val result = MenuController.rmCategory(request)
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome("/editCategory")
    }

    "edit Menu as Employee" in memDB {
      val request = FakeRequest(GET, "/editMenu").withSession(
        "role" -> "Mitarbeiter"
      )
      val result = MenuController.editMenu()(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Bereits in der Speisekarte")
    }

    "edit Menu as Customer" in memDB {
      val request = FakeRequest(GET, "/editMenu").withSession(
        "role" -> "Kunde"
      )
      val result = MenuController.editMenu()(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Sie haben nicht die nötigen Zugriffsrechte.")
    }

    "edit Category as Employee" in memDB {
      val request = FakeRequest(GET, "/editCategory").withSession(
        "role" -> "Mitarbeiter"
      )
      val result = MenuController.editCategory()(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Bereits in der Speisekarte")
    }

    "edit Category as Customer" in memDB {
      val request = FakeRequest(GET, "/editCategory").withSession(
        "role" -> "Kunde"
      )
      val result = MenuController.editCategory(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Sie haben nicht die nötigen Zugriffsrechte.")
    }

    "show showMenu View" in memDB {
      val request = FakeRequest(GET, "/showMenu")
      val result = MenuController.showMenu()(request)
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Willkommen zur Speisekarte")
    }
  }

}
