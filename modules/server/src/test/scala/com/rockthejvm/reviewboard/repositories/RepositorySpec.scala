package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*
import com.rockthejvm.reviewboard.syntax.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import org.postgresql.ds.PGSimpleDataSource

import javax.sql.DataSource
import org.testcontainers.containers.PostgreSQLContainer

trait RepositorySpec {

  val initScript: String

  /**   - \1. create a container
    *   - \2. create a datasource
    *   - \3. expose a layer
    */
  def createContainer(): PostgreSQLContainer[Nothing] = {
    val container: PostgreSQLContainer[Nothing] =
      new PostgreSQLContainer("postgres").withInitScript(initScript)
    container.start()
    container
  }

  def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val dataSource = new PGSimpleDataSource
    dataSource.setURL(container.getJdbcUrl)
    dataSource.setUser(container.getUsername)
    dataSource.setPassword(container.getPassword)
    dataSource
  }

  val dataSourceLayer: ZLayer[Scope, Throwable, DataSource] = ZLayer {
    ZIO
      .acquireRelease(
        // ZIO that returns the resource
        ZIO
          .attempt(createContainer())
          .tap(_ => ZIO.logInfo("Started PostgreSQL container"))
          .tapError(e => ZIO.logError(s"Failed to start container: ${e.getMessage}"))
      )(
        // will be called at the end of the program that requires this layer
        container =>
          ZIO
            .attempt(container.stop())
            .tap(_ => ZIO.logInfo("Stopped PostgreSQL container"))
            .tapError(e => ZIO.logError(s"Failed to stop container: ${e.getMessage}"))
            .ignore
      )
      .map(createDataSource)
  }
}
