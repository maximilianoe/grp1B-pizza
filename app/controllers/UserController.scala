package controllers

import forms.{CreateUserForm, EditUserForm, LoginUserForm, LongForm}
import play.api.data.Form
import play.api.data.Forms.{mapping, text, number, longNumber}
import play.api.mvc.{Action, AnyContent, Controller}
import services.UserService

/**
  * Controller for user specific operations.
  *
  * @author ob, scs, Maximilian Öttl
  */
object UserController extends Controller {

  /**
    * Form object for user data.
    */
  val userForm = Form(
    mapping(
      "Email" -> text.verifying("Bitte Ihre Email angeben", !_.isEmpty),
      "Passwort" -> text.verifying("Bitte Ihr Passwort angeben", !_.isEmpty),
      "Vorname" -> text.verifying("Bitte Ihren Vornamen angeben", !_.isEmpty),
      "Name" -> text.verifying("Bitte Ihren Namen angeben", !_.isEmpty),
      "Straße und Hausnummer" -> text.verifying("Bitte Ihre Straße und Hausnummer angeben", !_.isEmpty),
      "Postleitzahl" -> number,
      "Stadt" -> text.verifying("Bitte Ihre Stadt angeben", !_.isEmpty),
      "Rolle" -> text.verifying("Bitte Ihre Rolle angeben", !_.isEmpty)
    )(CreateUserForm.apply)(CreateUserForm.unapply))

  /**
    * Form object for login data.
    */
  val loginForm = Form(
    mapping(
      "Email" -> text,
      "Passwort" -> text
    )(LoginUserForm.apply)(LoginUserForm.unapply))

  /**
    * Form object for editUser data.
    */
  val editUserForm = Form(
    mapping(
      "Kunden-ID" -> longNumber,
      "Email" -> text.verifying("Bitte eine Email angeben", !_.isEmpty),
      "Passwort" -> text.verifying("Bitte ein Passwort angeben", !_.isEmpty),
      "Vorname" -> text.verifying("Bitte einen Vornamen angeben", !_.isEmpty),
      "Name" -> text.verifying("Bitte einen Namen angeben", !_.isEmpty),
      "Straße und Hausnummer" -> text.verifying("Bitte eine Straße und Hausnummer angeben", !_.isEmpty),
      "Postleitzahl" -> number,
      "Stadt" -> text.verifying("Bitte eine Stadt angeben", !_.isEmpty),
      "Rolle" -> text.verifying("Bitte eine Rolle angeben", !_.isEmpty)
    )(EditUserForm.apply)(EditUserForm.unapply))

  /**
    * Form object for deleteUser data.
    */
  val deleteUserForm = Form {
    mapping(
      "Kunden-ID" -> longNumber
    )(LongForm.apply)(LongForm.unapply)
  }

  /**
    * Adds a new user with the given data to the database.
    *
    * @return welcome page for new user
    */
  def addUser(): Action[AnyContent] = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.attemptFailed("badRequest"))
      },
      userData => {
        val deliveryTime = services.OrderService.calculateDeliveryTime(userData.zipcode)
        if (deliveryTime == -1) {
          Redirect(routes.UserController.attemptFailed("register"))
        } else {
          val user = services.UserService.addUser(userData.email, userData.password, userData.forename, userData.name, userData.address, userData.zipcode, userData.city, userData.role)
          if (user != null) {
            if (request2session.get("role").isDefined) {
              if (request2session.get("role").get == "Mitarbeiter") {
                Redirect(routes.UserController.attemptSuccessful("usercreated"))
              } else {
                Redirect(routes.UserController.attemptFailed("permissiondenied"))
              }
            } else {
              if (request2session.get("orderedProducts").isDefined) {
                Redirect(routes.BillController.setOrder(request2session.get("orderedProducts").get, request2session.get("sumOfOrder").get.toDouble)).withSession(
                  request.session +
                    ("user" -> user.id.toString) +
                    ("email" -> user.email.toString) +
                    ("forename" -> user.forename.toString) +
                    ("name" -> user.name.toString) +
                    ("address" -> user.address.toString) +
                    ("zipcode" -> user.zipcode.toString) +
                    ("city" -> user.city.toString) +
                    ("role" -> user.role.toString)
                )
              } else {
                Redirect(routes.UserController.welcomeUser()).withSession(
                  request.session +
                    ("user" -> user.id.toString) +
                    ("email" -> user.email.toString) +
                    ("forename" -> user.forename.toString) +
                    ("name" -> user.name.toString) +
                    ("address" -> user.address.toString) +
                    ("zipcode" -> user.zipcode.toString) +
                    ("city" -> user.city.toString) +
                    ("role" -> user.role.toString)
                )
              }
            }
          } else {
            Redirect(routes.UserController.attemptFailed("emailused"))
          }
        }
      })
  }

  /**
    * Edits a specified user with the given data from the database.
    *
    * @return success of edit
    */
  def editUser: Action[AnyContent] = Action { implicit request =>
    editUserForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.attemptFailed("badRequest"))
      },
      userData => {
        val deliveryTime = services.OrderService.calculateDeliveryTime(userData.zipcode)
        if (deliveryTime == -1) {
          Redirect(routes.UserController.attemptFailed("register"))
        } else {
          val user = services.UserService.editUser(userData.customerID, userData.email, userData.password, userData.forename, userData.name, userData.address, userData.zipcode, userData.city, userData.role)
          if (user != null) {
            Redirect(routes.UserController.attemptSuccessful("useredited"))
          } else {
            Redirect(routes.UserController.attemptFailed("emailused"))
          }
        }
      })
  }

  /**
    * Deletes a specified user from the database.
    *
    * @return success of deletion
    */
  def deleteUser(): Action[AnyContent] = Action { implicit request =>
    deleteUserForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.attemptFailed("badRequest"))
      },
      userData => {
        services.UserService.deleteUser(userData.value)
        Redirect(routes.UserController.attemptSuccessful("userdeleted"))
      })
  }

  /**
    * Shows the welcome view for a customer.
    */
  def welcomeUser: Action[AnyContent] = Action { implicit request =>
    if (request2session.get("role").get == "Mitarbeiter") {
      Redirect(routes.UserController.welcomeEmployee())
    } else {
      Ok(views.html.welcomeUser())
    }
  }

  /**
    * Shows the welcome view for an employee.
    */
  def welcomeEmployee: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.welcomeEmployee())
  }

  /**
    * Shows the attemptFail view with a given errorcode.
    */
  def attemptFailed(errorcode: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.attemptFailed(errorcode))
  }

  /**
    * Shows the attemptSuccessful view with a given successcode.
    */
  def attemptSuccessful(successcode: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.attemptSuccessful(successcode))
  }

  /**
    * Lists all users in the database.
    */
  def showUsers: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.showUsers(UserService.registeredUsers))
  }

  /**
    * Logs in a user with the given data.
    *
    * @return welcome page for loggedin user
    */
  def loginUser: Action[AnyContent] = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.attemptFailed("badRequest"))
      },
      loginData => {
        val trylogin = services.UserService.loginUser(loginData.email, loginData.password)
        trylogin match {
          case Left("invalid") => Redirect(routes.UserController.attemptFailed("loginfailed"))
          case Left("inactive") => Redirect(routes.UserController.attemptFailed("inactive"))
          case _ =>
            val user = trylogin.right.get
            if (request2session.get("orderedProducts").isDefined) {
              Redirect(routes.BillController.setOrder(request2session.get("orderedProducts").get, request2session.get("sumOfOrder").get.toDouble)).withSession(
                request.session +
                  ("user" -> user.id.toString) +
                  ("email" -> user.email.toString) +
                  ("forename" -> user.forename.toString) +
                  ("name" -> user.name.toString) +
                  ("address" -> user.address.toString) +
                  ("zipcode" -> user.zipcode.toString) +
                  ("city" -> user.city.toString) +
                  ("role" -> user.role.toString)
              )
            } else {
              Redirect(routes.UserController.welcomeUser()).withSession(
                request.session +
                  ("user" -> user.id.toString) +
                  ("email" -> user.email.toString) +
                  ("forename" -> user.forename.toString) +
                  ("name" -> user.name.toString) +
                  ("address" -> user.address.toString) +
                  ("zipcode" -> user.zipcode.toString) +
                  ("city" -> user.city.toString) +
                  ("role" -> user.role.toString)
              )
            }
        }
      })
  }

  /**
    * Logs out the currently logged in user.
    *
    * @return index
    */
  def logoutUser: Action[AnyContent] = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  /**
    * Shows the editUsers view for employees.
    */
  def editUsers: Action[AnyContent] = Action { implicit request =>
    if (request2session.get("role").get == "Mitarbeiter") {
      Ok(views.html.editUsers(services.UserService.registeredUsers, controllers.UserController.userForm, controllers.UserController.editUserForm, controllers.UserController.deleteUserForm))
    } else {
      Ok(views.html.attemptFailed("permissiondenied"))
    }
  }
}