package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.Company
import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

import java.util.UUID

trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getAll(): Task[List[Company]]
}

class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {
  import quill.*
  // each row in the "companies" table maps to a Company case class instance
  inline given schema: SchemaMeta[Company]       = schemaMeta[Company]("companies") // for reading only
  // for insert and update, specify which columns to exclude
  inline given insertSchema: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given updateSchema: UpdateMeta[Company] = updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(r => r)
    }

  // UPDATE companies SET ... = ... WHERE id = ...
  override def update(id: Long, op: Company => Company): Task[Company] =
    for {
      company <- getById(id).someOrFail(new RuntimeException(s"Cannot update with missing ID $id"))
      result  <- run {
                   query[Company]
                     .filter(_.id == lift(id))
                     .updateValue(lift(op(company)))
                     .returning(r => r)
                 }
    } yield result

  override def delete(id: Long): Task[Company] =
    for {
      company <- getById(id).someOrFail(new RuntimeException(s"Cannot delete with missing ID $id"))
      result  <- run {
                   query[Company]
                     .filter(_.id == lift(id))
                     .delete
                     .returning(r => r)
                 }
    } yield result

  override def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.id == lift(id))
//        .take(1) // only adds "LIMIT 1" to the query, not necessary
        .value
    }

  override def getAll(): Task[List[Company]] = run(query[Company])

}

object CompanyRepositoryLive extends ZIOAppDefault {
  val layer = ZLayer.fromFunction(new CompanyRepositoryLive(_))

  val program = for {
    repo        <- ZIO.service[CompanyRepository]
    companyName <-
      ZIO.succeed(s"Company-${UUID.randomUUID().toString}") // generate a random company name
    slug        <- ZIO.succeed(Company.makeSlug(companyName))
    _           <- repo.create(
                     Company.dummy.copy(name = companyName, slug = slug, url = s"https://example.com/${slug}")
                   ) // create a dummy company
    list        <- repo.getAll() // get all companies
    _           <- Console.printLine(s"Companies at the end: ${list.mkString(", ")}")
  } yield ()

  override def run = program
    .provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase),
      Quill.DataSource.fromPrefix("rockthejvm.db") // provide the Quill context with
    )
}
