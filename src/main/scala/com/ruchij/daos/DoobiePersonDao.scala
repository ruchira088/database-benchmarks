package com.ruchij.daos

import cats.Applicative
import com.ruchij.models.Person
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator
import DoobieMappings._

import java.util.UUID

object DoobiePersonDao extends PersonDao[ConnectionIO] {
  val SelectQuery =
    fr"SELECT id, created_at, username, first_name, last_name, age FROM person"

  override def insert(person: Person): ConnectionIO[Int] =
    sql"""
      INSERT INTO person (id, created_at, username, first_name, last_name, age)
        VALUES (
          ${person.id},
          ${person.createdAt},
          ${person.username},
          ${person.firstName},
          ${person.lastName},
          ${person.age}
        )
    """
      .update
      .run

  override def findById(userId: UUID): ConnectionIO[Option[Person]] =
    (SelectQuery ++ fr"WHERE id = $userId").query[Person].option

  override def findByUsername(username: String): ConnectionIO[Option[Person]] =
    (SelectQuery ++ fr"WHERE username = $username").query[Person].option

  override def searchByFirstName(firstName: String, offset: Int, pageSize: Int): ConnectionIO[List[Person]] =
    (SelectQuery ++ fr"WHERE first_name = $firstName OFFSET ${offset * pageSize} LIMIT $pageSize")
      .query[Person]
      .to[List]

  override def searchByLastName(lastName: String, offset: Int, pageSize: Int): ConnectionIO[List[Person]] =
    (SelectQuery ++ fr"WHERE last_name = $lastName OFFSET ${offset * pageSize} LIMIT $pageSize")
      .query[Person]
      .to[List]

  override def deleteById(userId: UUID): ConnectionIO[Option[Person]] =
    findById(userId)
      .flatMap {
        maybePerson =>
          maybePerson.fold[ConnectionIO[Option[Person]]](Applicative[ConnectionIO].pure(None)) {
            person =>
              sql"DELETE FROM person WHERE id = $userId".update.run
                .map {
                  case 0 => None

                  case _ => Some(person)
                }
          }
      }

  override val count: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM person".query[Long].unique
}
