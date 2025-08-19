package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

import com.rockthejvm.reviewboard.syntax.*
import com.rockthejvm.reviewboard.domain.*

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  override val initScript = "companies.sql"

  override def spec =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          company <- repo.create(Company.dummy)
        } yield company

        program.must(_ == Company.dummy)
      },
      test("retrieve all companies") {
        val program = for {
          repo <- ZIO.service[CompanyRepository]
          _ <- repo.create(Company.dummy)
          companies <- repo.getAll()
        } yield companies

        program.must(_ == List(Company.dummy))
      }
    ).provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase),
      // data source is different - fetched from the test container
      dataSourceLayer,
      // infra
      Scope.default
    )
}
