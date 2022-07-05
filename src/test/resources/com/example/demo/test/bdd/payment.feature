Feature: Payment
  Scenario: Successful Payment
    Given the following accounts exist:
      | id | currency | balance |
      | A  | PHP      | 500.00  |
      | B  | PHP      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "A" to merchant "X"
    Then the payment is accepted
    And the payment is saved
    And the payment has 2 transaction entries
    And the payment has a DEBIT transaction entry of PHP 50.00 on account "A"
    And the payment has a CREDIT transaction entry of PHP 50.00 on account "B"
    And the account "A" balance is PHP 450.00
    And the account "B" balance is PHP 300.00

  Scenario: Failed Payment due to Non-existent Account
    Given the following accounts exist:
      | id | currency | balance |
      | A  | PHP      | 500.00  |
      | B  | PHP      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "C" to merchant "X"
    Then the payment is unprocessable
    And the payment error code is "SourceAccountNotFoundException"
    And the payment error message is "Source account not found"

  Scenario: Failed Payment due to Non-existent Merchant
    Given the following accounts exist:
      | id | currency | balance |
      | A  | PHP      | 500.00  |
      | B  | PHP      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "A" to merchant "Y"
    Then the payment is unprocessable
    And the payment error code is "MerchantNotFoundException"
    And the payment error message is "Merchant not found"

  Scenario: Failed Payment due to Inconsistent Currency with Account
    Given the following accounts exist:
      | id | currency | balance |
      | A  | JPY      | 500.00  |
      | B  | PHP      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "A" to merchant "X"
    Then the payment is unprocessable
    And the payment error code is "InconsistentCurrencyException"
    And the payment error message is "Request currency does not match account or merchant account currency"

  Scenario: Failed Payment due to Inconsistent Currency with Merchant Account
    Given the following accounts exist:
      | id | currency | balance |
      | A  | PHP      | 500.00  |
      | B  | JPY      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "A" to merchant "X"
    Then the payment is unprocessable
    And the payment error code is "InconsistentCurrencyException"
    And the payment error message is "Request currency does not match account or merchant account currency"

  Scenario: Failed Payment due to Insufficient Balance
    Given the following accounts exist:
      | id | currency | balance |
      | A  | PHP      | 25.00  |
      | B  | PHP      | 250.00  |
    And the following merchants exist:
      | id | account id |
      | X  | B          |
    When the client pays PHP 50.00 using account "A" to merchant "X"
    Then the payment is unprocessable
    And the payment error code is "InsufficientBalanceException"
    And the payment error message is "Insufficient balance"