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

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helptosavestub.controllers.NSIGetAccountBehaviour.NSIGetAccountByNinoResponse
import uk.gov.hmrc.helptosavestub.models.NSIUserInfo
import uk.gov.hmrc.helptosavestub.support.AkkaMaterializerSpec
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class NSIControllerSpec extends UnitSpec with WithFakeApplication with AkkaMaterializerSpec {

  import NSIUserInfo._

  val testCreateAccount = NSIUserInfo(
    "Donald", "Duck", LocalDate.of(1990, 1, 1), "AA999999A", // scalastyle:ignore magic.number
                      ContactDetails("1", ",Test Street 2", None, None, None, "BN124XH", Some("GB"), Some("dduck@email.com"), None, "02"),
    "online")

  val authHeader = {
    val encoded = new String(Base64.getEncoder().encode("username:password".getBytes(StandardCharsets.UTF_8)))
    "Authorization-test" → s"Basic: $encoded"
  }

  "Post /nsi-services/account  " should {
    "return a successful Create Account" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.createAccount()(request)
      status(result) shouldBe CREATED
    }

    "return a 401  UNAUTHORIZED" in {
      val request = FakeRequest()
        .withJsonBody(Json.toJson(testCreateAccount))
      val result = NSIController.createAccount()(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 for a bad request in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.createAccount()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "Put /create-account  " should {
    "return a successful successful status" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
        .withJsonBody(Json.toJson(testCreateAccount))

      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe OK
    }

    "return a 401  UNAUTHORIZED" in {
      val request = FakeRequest()
        .withJsonBody(Json.toJson(testCreateAccount))
      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe UNAUTHORIZED
    }

    "return a 400 for a bad request in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.updateEmailOrHealthCheck()(request)
      status(result) shouldBe BAD_REQUEST
    }
  }

  "Get /nsi-services/account" should {
    "return a successful status when given an existing nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM000001A"), Some("V1.0"))(request)
      status(result) shouldBe OK
      val json = contentAsString(result)
      Json.fromJson[NSIGetAccountByNinoResponse](Json.parse(json)).get shouldBe NSIGetAccountByNinoResponse.bethNSIResponse(Some("correlationId"))
    }

    "return a 400 with errorMessageId HTS-API015-002 when the service version is missing" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EM000001A"), None)(request))
      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "error" \ "errorMessageId").as[String] shouldBe "HTS-API015-002"
      (jsonBodyOf(result) \ "correlationId").as[String] shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-003 when given an unsupported service version" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EM000001A"), Some("V1.5"))(request))
      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "error" \ "errorMessageId").as[String] shouldBe "HTS-API015-003"
      (jsonBodyOf(result) \ "correlationId").as[String] shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-004 when not given a nino" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), None, Some("V1.0"))(request))
      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "error" \ "errorMessageId").as[String] shouldBe "HTS-API015-004"
      (jsonBodyOf(result) \ "correlationId").as[String] shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-005 when given a nino in the incorrect format" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ00000A"), Some("V1.0"))(request))
      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "error" \ "errorMessageId").as[String] shouldBe "HTS-API015-005"
      (jsonBodyOf(result) \ "correlationId").as[String] shouldBe "correlationId"
    }

    "return a 400 with errorMessageId HTS-API015-006 when given a nino not found" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = await(NSIController.getAccount(Some("correlationId"), Some("EZ000001A"), Some("V1.0"))(request))
      status(result) shouldBe BAD_REQUEST
      (jsonBodyOf(result) \ "error" \ "errorMessageId").as[String] shouldBe "HTS-API015-006"
      (jsonBodyOf(result) \ "correlationId").as[String] shouldBe "correlationId"
    }

    "return a 500 when given a nino with 500 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM500001A"), Some("V1.0"))(request)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }

    "return a 401 when given a nino with 401 in" in {
      val request = FakeRequest()
        .withHeaders(authHeader)
      val result = NSIController.getAccount(Some("correlationId"), Some("EM000401A"), Some("V1.0"))(request)
      status(result) shouldBe UNAUTHORIZED
    }

  }
}
