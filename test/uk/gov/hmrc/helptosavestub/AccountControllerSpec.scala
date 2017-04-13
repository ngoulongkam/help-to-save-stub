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

package uk.gov.hmrc.helptosavestub

import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.helptosavestub.connectors.NSIConnector
import uk.gov.hmrc.helptosavestub.controllers.AccountController
import uk.gov.hmrc.helptosavestub.models.AccountDetails
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AccountControllerSpec extends UnitSpec with WithFakeApplication {

  private val body = """{"accountName":"test","sortCode":"12-34-56","accountNumber":"02045676"}"""

  val request = FakeRequest("POST", "/create-account")
    .withJsonBody(Json.parse(body))
    .withHeaders(("Content-Type", "application/json"))

  "POST /create-account" should {
    "return 200" in {
      val result = new AccountController(new MockNSIConnector).createAccount()(request)
      status(result) shouldBe OK
      val responseJson = contentAsString(result)
      responseJson should be(body)
    }

  }

  class MockNSIConnector extends NSIConnector {
    override def createAccount(accountDetails: AccountDetails)(implicit hc: HeaderCarrier): Future[AccountDetails] = {
      Future.successful(accountDetails)
    }
  }

}
