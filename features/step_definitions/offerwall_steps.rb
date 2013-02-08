Given /^(?:that |)I launch the OfferWall$/ do 
  launch_offerwall
  sleep 2
end

And /^I wait for the OfferWall to become visible$/ do
  wait_for_offerwall
  sleep 1
end

Then /^I should see some offers$/ do
  screenshot_and_raise "No offers available" unless get_offers_in_page > 0
  sleep 1
end

Then /^I click on the support button$/ do
  touch_support_button
  sleep 5
end

Then /^I click on the privacy button$/ do
  touch_privacy_button
  sleep 5
end

Then /^I click on the help button$/ do
  touch_help_button
  sleep 5
end

Then /^I click on the back button$/ do
  touch_back_button
  sleep 5
end

Given /^that offer with LPID (-?\d+) is available$/ do |lpid|
  screenshot_and_raise "Offer is not available" unless offer_with_lpid? lpid.to_i, true
  sleep 2
end

Then /^I press offer with LPID (-?\d+)$/ do |lpid|
  touch_offer_with_lpid lpid.to_i, true
  sleep 2
end

Then /^the OFW currency is "([^\"]*)"$/ do |currency|
  screenshot_and_raise "Currency is not #{currency}" unless get_currency('.currency') == currency
end