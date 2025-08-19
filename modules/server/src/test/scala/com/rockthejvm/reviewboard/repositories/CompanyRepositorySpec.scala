package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import zio.test.Assertion.equalTo
import com.rockthejvm.reviewboard.syntax.*
import com.rockthejvm.reviewboard.domain.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

object CompanyRepositorySpec extends ZIOSpecDefault, RepositorySpec {

  override val initScript = "companies.sql"

  override def spec =
    suite("CompanyRepositorySpec")(
      test("create company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(Company.dummy)
        } yield company
        program.must(_ == Company.dummy)
      }
    ).provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy[SnakeCase](SnakeCase),
      // test datasource layer using a test docker container here
      dataSourceLayer,
      // infra
      Scope.default
    )
}
