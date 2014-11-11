package dbaccess

import anorm._
import play.api.Play.current
import play.api.db._
import anorm.NamedParameter.symbol
import models.User

/**
 * Data access object for user related operations.
 * 
 * @author ob, scs
 */
object UserDao {

  /**
   * Creates the given user in the database.
   * @param user the user object to be stored.
   * @return the persisted user object 
   */
  def addUser(user: User): User = {
    DB.withConnection { implicit c =>
      val id: Option[Long] =
        SQL("insert into User(name) values ({name})").on(
          'name -> user.name).executeInsert()
      user.id = id.get
    }
    return user
  }

  /**
   * Returns a list of available user from the database.
   * @return a list of user objects.
   */
  def registeredUsers: List[User] = {
    DB.withConnection { implicit c =>
      val selectUsers = SQL("Select id, name from User;")
      // Transform the resulting Stream[Row] to a List[(String,String)]
      val users = selectUsers().map(row => User(row[Long]("id"), row[String]("name"))).toList
      return users;
    }
  }

}