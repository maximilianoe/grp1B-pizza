package models

/**
  * User entity.
  *
  * @param id   database id of the user.
  * @param name name of the user.
  */
case class User(var id: Long, var email: String, var password: String, var forename: String, var name: String, var address: String, var zipcode: Int, var city: String, var role: String, var inactive: Boolean)
