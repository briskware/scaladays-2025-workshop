package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.Company
import com.rockthejvm.reviewboard.http.errors.HttpError
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.*
import sttp.tapir.EndpointInput.AuthType.Http
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

class CompanyEndpoints {
  // GET /api/companies
  val getAllEndpoint: Endpoint[Unit, Unit, Throwable, List[Company], Any] =
    endpoint.get
      .prependIn("api")             // /api
      .in("companies")              // /api/companies
      .out(jsonBody[List[Company]]) // data type must have a given JsonCodec
      .errorOut(
        statusCode and plainBody[String]
      )                             // if http server throws an error, we return a status code and a string message
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode) // convert errors to HttpError
      //                          ^^^^^  if the server fails => map the error to a HttpError

      // for docs (swagger) purposes
      .name("getAll")
      .tag("companies")
      .description("Get all companies")

  // GET /api/companies/{id} -> a single company by ID
  // .in("companies") / path[String]("id")
  val getByIdEndpoint: Endpoint[Unit, Int, Throwable, Company, Any] =
    endpoint.get
      .prependIn("api")
      .in("companies" / path[Int]("id")) // /api/companies/{id}
      .out(jsonBody[Company])
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .name("getById")
      .tag("companies")
      .description("Get a company by ID")

  // POST /api/companies -> create a new company from a JSON body { name, url }
  val createEndpoint: Endpoint[Unit, CreateCompanyRequest, Throwable, Company, Any] =
    endpoint.post
      .prependIn("api")
      .in("companies") // /api/companies
      // the request body is a JSON body that maps to a CreateCompanyRequest
      .in(jsonBody[CreateCompanyRequest])
      // the response body is a JSON body that maps to a Company
      .out(jsonBody[Company])
      .errorOut(statusCode and plainBody[String])
      .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
      .name("create")
      .tag("companies")
      .description("Create a new company")

}
