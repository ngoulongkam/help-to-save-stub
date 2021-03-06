/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.helptosavestub.controllers.BARSController.{BARSResponse, BankDetails}
import uk.gov.hmrc.helptosavestub.controllers.BankDetailsBehaviour.{CreateAccountResponse, Profile}
import uk.gov.hmrc.helptosavestub.models.{ErrorDetails, NSIErrorResponse}

trait BankDetailsBehaviour {

  def getBankProfile(bankDetails: BankDetails): Profile = {
    val accountNumberWithSortCodeIsValid =
      if (bankDetails.accountNumber.startsWith("9")) {
        Some(false)
      } else if (bankDetails.accountNumber.startsWith("5")) {
        None
      } else {
        Some(true)
      }

    val sortCodeIsPresentOnEISCD =
      if (bankDetails.sortCode.startsWith("9")) {
        "no"
      } else if (bankDetails.sortCode.startsWith("5")) {
        "whoopsie"
      } else {
        "yes"
      }

    val barsResponse =
      accountNumberWithSortCodeIsValid.map(a ⇒ BARSResponse(a, sortCodeIsPresentOnEISCD))

    if (bankDetails.accountNumber.startsWith("707")) {
      Profile(barsResponse, CreateAccountResponse(Left(NSIErrorResponse.incorrectAccountNumber)))
    } else if (bankDetails.sortCode.startsWith("70-3")) {
      Profile(barsResponse, CreateAccountResponse(Left(NSIErrorResponse.incorrectSortCode)))
    } else {
      Profile(barsResponse, CreateAccountResponse(Right(())))
    }
  }

}

object BankDetailsBehaviour {

  case class CreateAccountResponse(response: Either[ErrorDetails, Unit])

  case class Profile(barsResponse: Option[BARSResponse], createAccountResponse: CreateAccountResponse)

}
