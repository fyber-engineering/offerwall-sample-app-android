And /^that I am on the items page$/ do 
  goto_items
  sleep 1
end

Given /^I open unlock offerwall$/ do
  launch_unlock_offerwall
  sleep 1
end

Then /^I get an error for invalid item id$/ do
  raise_and_screenshot "Error" unless invalid_unlock_item_id?
end

Given /^I open unlock offerwall for item "([^\"]*)"$/ do |itemId|
  launch_unlock_offerwall itemId
end

Given /^I request item status$/ do
  request_unlock_items
  sleep 1
end
