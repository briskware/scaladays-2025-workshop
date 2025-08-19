package com.rockthejvm.reviewboard.repositories

import zio.*

import org.testcontainers.containers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

trait RepositorySpec {
  // provide a data access layer to a Docker container

  val initScript: String // to be supplied by every class that extends this

  def createContainer() = {
    val container: PostgreSQLContainer[Nothing] =
      PostgreSQLContainer("postgres").withInitScript(initScript)

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

  /**
   * 1. create a container - DONE
   * 2. create a DataSource - DONE
   * 3. expose the layer
   * 4. close the container when done
   */
  val dataSourceLayer: ZLayer[Scope, Throwable, DataSource] =
    ZLayer {
      ZIO.acquireRelease(
        /* ZIO that returns the resource */
        ZIO.attempt(createContainer())
      )(
        /* ZIO effect that releases the resource */
        // will be called at the end of the program that requires this layer
        container => ZIO.attempt(container.stop())
          .tapError(e => ZIO.logError(e.getMessage))
          .ignore
      ).map(createDataSource)
    }
}
