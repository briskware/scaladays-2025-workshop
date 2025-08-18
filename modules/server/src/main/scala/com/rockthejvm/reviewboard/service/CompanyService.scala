package com.rockthejvm.reviewboard.service

import zio.*

import com.rockthejvm.reviewboard.domain.*
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
}

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService {
  def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(
      Company(
        -1,
        Company.makeSlug(req.name),
        name = req.name,
        url = req.url
      )
    )

  def getAll(): Task[List[Company]] =
    repo.getAll()

  def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)
}

object CompanyServiceLive {
  val layer = ZLayer.fromFunction(new CompanyServiceLive(_))
}