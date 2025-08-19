package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.domain.Company
import com.rockthejvm.reviewboard.http.errors.HttpError
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest

class CompanyEndpoints extends BaseEndpoint {
  // GET /api/companies -> List[Company]
  val getAllEndpoint =
    baseEndpoint
      // type
      .get
      // path
      .in("companies") // /companies
      // input-output
      .out(jsonBody[List[Company]]) // data type must have a given JsonCodec[Company]
      .name("getAll")
      .tag("companies")
      .description("get all companies in the application")

  // GET /api/companies/id -> a single company by its id
  val getByIdEndpoint =
    baseEndpoint
      .get
      .in("companies" / path[String]("id")) // /companies
      .out(jsonBody[Option[Company]])
      .name("getById")
      .tag("companies")
      .description("get company by id")

  // POST /api/companies {name, url} -> the company added
  val createEndpoint =
    baseEndpoint
      .post
      .in("companies")
      .in(jsonBody[CreateCompanyRequest])
      .out(jsonBody[Company])
      .name("create")
      .tag("companies")
      .description("create new company")

  val createPremiumEndpoint =
    baseEndpoint
      .post
      .in("companies" / "premium")
      .in(jsonBody[CreateCompanyRequest])
      .out(plainBody[String])
      .name("createPremium")
      .tag("companies")
      .description("create new PREMIUM company with a tag!!!")


  val webhookEndpoint =
    baseEndpoint
      .in("webhook")
      .post
      .in(stringBody) // not jsonBody[String] because I will parse the webhook payload with the Stripe API
      .in(header[String]("Stripe-Signature"))
}


