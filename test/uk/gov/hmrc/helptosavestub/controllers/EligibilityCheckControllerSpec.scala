/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.helptosavestub.controllers

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.helptosavestub.controllers.EligibilityCheckController.EligibilityCheckResult
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class EligibilityCheckControllerSpec extends UnitSpec with WithFakeApplication {

  val fakeRequest = FakeRequest("GET", "/").withHeaders("Authorization" → "Bearer test")

  val eligCheckController = new EligibilityCheckController

  "GET /" should {

    "returns true when user is eligible" in {
      verifyEligibility("EL071111D", 1)
    }

    "returns false when user is not eligible for reason code 2" in {
      verifyEligibility("NE021111D", 2)
    }

    "returns false when user is not eligible for reason code 3" in {
      verifyEligibility("NE031111D", 2)
    }

    "returns false when user already has an account" in {
      verifyEligibility("AC111111D", 3)
    }

      def verifyEligibility(nino:            String,
                            resultCode:      Int,
                            ucClaimant:      Option[String] = None,
                            withinThreshold: Option[String] = None): Unit = {

        val result = eligCheckController.eligibilityCheck(nino, ucClaimant, withinThreshold)(fakeRequest)
        status(result) shouldBe Status.OK
        val json = contentAsString(result)
        val expected = resultCode match {
          case 1 ⇒ "Eligible to HtS Account"
          case 2 ⇒ "Ineligible to HtS Account"
          case 3 ⇒ "HtS account already exists"
          case _ ⇒ sys.error("Invalid result code")
        }

        Json.fromJson[EligibilityCheckResult](Json.parse(json)).get.result shouldBe expected

      }
  }
}
