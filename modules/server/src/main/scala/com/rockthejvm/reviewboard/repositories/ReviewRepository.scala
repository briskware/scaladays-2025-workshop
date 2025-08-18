package com.rockthejvm.reviewboard.repositories

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import com.rockthejvm.reviewboard.domain.*

// CRUD methods
trait ReviewRepository {
  def create(company: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
}

class ReviewRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {
  import quill.* // adds the Quill API

  inline given schema: SchemaMeta[Review] = schemaMeta[Review]("reviews") // read
  // each row in the "companies" table is parsable as a Company cas class instance
  inline given insSchema: InsertMeta[Review] = insertMeta[Review](_.id) // insert
  inline given upSchema: UpdateMeta[Review] = updateMeta[Review](_.id) // update

  def create(review: Review): Task[Review] =
    run {
      query[Review]
        .insertValue(lift(review))
        .returning(r => r)
    }

  def getById(id: Long): Task[Option[Review]] =
    run {
      query[Review]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByCompanyId(id: Long) =
    run {
      query[Review]
        .filter(_.companyId == lift(id))
    }

}

object ReviewRepositoryLive {
  val layer = ZLayer.fromFunction(new ReviewRepositoryLive(_))
}
