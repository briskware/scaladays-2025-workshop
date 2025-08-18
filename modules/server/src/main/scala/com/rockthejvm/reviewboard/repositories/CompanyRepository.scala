package com.rockthejvm.reviewboard.repositories

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import com.rockthejvm.reviewboard.domain.*

// CRUD methods
trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getAll(): Task[List[Company]]
}

class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {
  import quill.* // adds the Quill API

  inline given schema: SchemaMeta[Company] = schemaMeta[Company]("companies") // read
  // each row in the "companies" table is parsable as a Company cas class instance
  inline given insSchema: InsertMeta[Company] = insertMeta[Company](_.id) // insert
  inline given upSchema: UpdateMeta[Company] = updateMeta[Company](_.id) // update

  def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(r => r)
    }

  // UPDATE companies SET ... = ... WHERE id = __
  def update(id: Long, op: Company => Company): Task[Company] =
    for {
      company <- getById(id).someOrFail(new RuntimeException(s"cannot update: missing id $id"))
      result <- run {
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(op(company)))
          .returning(r => r)
      }
    } yield result

  def getAll(): Task[List[Company]] =
    run(query[Company])

  def delete(id: Long): Task[Company] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .delete
        .returning(r => r)
    }

  def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .take(1)
        .value
    }
}

object CompanyRepositoryLive extends ZIOAppDefault {

  val layer = ZLayer.fromFunction(new CompanyRepositoryLive(_))

  val program = for {
    repo <- ZIO.service[CompanyRepository]
    _ <- repo.create(Company.dummy)
    list <- repo.getAll()
    _ <- Console.printLine(s"Found companies: $list")
  } yield ()

  override def run =
    program.provide(
      layer,
      // infra
      Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase), // SnakeCase.type
      Quill.DataSource.fromPrefix("rockthejvm.db")
    )
}
