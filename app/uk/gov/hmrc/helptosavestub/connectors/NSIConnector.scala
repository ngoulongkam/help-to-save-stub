/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helptosavestub.connectors

import com.google.inject.{ImplementedBy, Singleton}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.helptosavestub.WSHttp
import uk.gov.hmrc.helptosavestub.models.AccountDetails
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

@ImplementedBy(classOf[NSIConnectorImpl])
trait NSIConnector {

  def createAccount(accountDetails: AccountDetails)(implicit hc: HeaderCarrier): Future[AccountDetails]

}

@Singleton
class NSIConnectorImpl extends NSIConnector with ServicesConfig {

  private val nsiRoot = baseUrl("help-to-save-nsi-stub")

  private val serviceURL = "help-to-save-nsi-stub/create-account"

  private val http = WSHttp

  case class AccountCreationException(accountDetails: AccountDetails) extends Exception(s"Could not parse user details $accountDetails ")

  override def createAccount(accountDetails: AccountDetails)(implicit hc: HeaderCarrier): Future[AccountDetails] =
    http.POST(s"$nsiRoot/$serviceURL", accountDetails).flatMap {
      _.json.validate[AccountDetails] match {
        case JsSuccess(aDetails, _) ⇒ Future.successful(aDetails)
        case JsError(_) ⇒ Future.failed[AccountDetails](AccountCreationException(accountDetails))
      }
    }

}
