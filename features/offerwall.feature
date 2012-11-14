Feature: Offerwall feature
    
  Scenario: The user can launch the OfferWall
    Given that user "tester" has valid credentials for "1246"
	And that user launch the offerwall

  Scenario: Opening offerwall without credentials
    Given that user launch the offerwall
	

     