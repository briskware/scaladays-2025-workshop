package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import com.rockthejvm.reviewboard.domain.Company
import com.rockthejvm.reviewboard.common.Constants

object CompanyComponents {
  def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceholder),
      alt := company.name
    )

  def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  def fullLocationString(company: Company): String =
    (company.location, company.country) match {
      case (Some(location), Some(country)) => s"$location, $country"
      case (Some(location), None)          => location
      case (None, Some(country))           => country
      case (None, None)                    => "N/A"
    }

  def renderOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", fullLocationString(company)),
      renderDetail("tags", company.tags.mkString(", "))
    )

  private def renderAction(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href := company.url,
        target := "blank",
        button(
          `type` := "button",
          cls := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(company: Company) =
    div(
      cls := "jvm-recent-companies-cards position-relative", // Added position-relative for badge positioning
      Option(company.premium).filter(identity).toList.map(_ =>
        div(
          cls := "premium-badge",
          styleAttr := "position: absolute; top: 10px; right: 10px; background-color: #FFD700; color: #000; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 0.8rem; z-index: 1;",
          "PREMIUM"
        )
      ),
      div(
        cls := "jvm-recent-companies-card-img",
        CompanyComponents.renderCompanyPicture(company)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            company.name,
            s"/company/${company.id}",
            "company-title-link"
          )
        ),
        CompanyComponents.renderOverview(company)
      ),
      renderAction(company)
    )
}
