Feature: Offerwall feature
  
  Background:
    Given that user "tester" has valid credentials for "1246"
    
  Scenario: The user can launch the OfferWall  
	Given that user launch the offerwall
	And that the offerwall is visible

  Scenario: Opening offerwall without credentials
	And that the offerwall is visible
    Given that user launch the offerwall
	

     