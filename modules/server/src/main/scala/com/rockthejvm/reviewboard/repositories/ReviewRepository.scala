package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.Review
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait ReviewRepository {
  def create(company: Review): Task[Review]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getAllByCompanyId(id: Long): Task[List[Review]]
}

class ReviewRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {
  import quill.*

  inline given schema: SchemaMeta[Review]       = schemaMeta[Review]("reviews")
  inline given insertSchema: InsertMeta[Review] = insertMeta[Review](_.id)
  inline given updateSchema: UpdateMeta[Review] = updateMeta[Review](_.id)

  override def create(review: Review): Task[Review] =
    run {
      query[Review]
        .insertValue(lift(review))
        .returning(r => r)
    }

  override def update(id: Long, op: Review => Review): Task[Review] =
    for {
      review <- getById(id).someOrFail(new RuntimeException(s"Cannot update with missing ID $id"))
      result <- run {
                  query[Review]
                    .filter(_.id == lift(id))
                    .updateValue(lift(op(review)))
                    .returning(r => r)
                }
    } yield result

  override def delete(id: Long): Task[Review] =
    for {
      review <- getById(id).someOrFail(new RuntimeException(s"Cannot delete with missing ID $id"))
      result <- run {
                  query[Review]
                    .filter(_.id == lift(id))
                    .delete
                    .returning(r => r)
                }
    } yield result

  override def getById(id: Long): Task[Option[Review]] =
    run {
      query[Review]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getAllByCompanyId(id: Long): Task[List[Review]] =
    run {
      query[Review]
        .filter(_.companyId == lift(id))
    }
}

object ReviewRepositoryLive {
  val layer: URLayer[Quill.Postgres[SnakeCase], ReviewRepository] =
    ZLayer.fromFunction(new ReviewRepositoryLive(_))
}
