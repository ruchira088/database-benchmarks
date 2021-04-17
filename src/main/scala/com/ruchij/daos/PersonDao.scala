package com.ruchij.daos

import com.ruchij.models.Person

import java.util.UUID

trait PersonDao[F[_]] {

  def insert(person: Person): F[Int]

  def findById(userId: UUID): F[Option[Person]]

  def findByUsername(username: String): F[Option[Person]]

  def searchByFirstName(firstName: String, offset: Int, pageSize: Int): F[List[Person]]

  def searchByLastName(lastName: String, offset: Int, pageSize: Int): F[List[Person]]

  def deleteById(userId: UUID): F[Option[Person]]

  val count: F[Long]

}