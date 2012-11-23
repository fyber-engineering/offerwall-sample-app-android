And /^that I am on the items page$/ do 
  goto_items
  sleep 1
end

Given /^I open unlock offerwall$/ do
  launch_unlock_offerwall
  sleep 1
end

Then /^I get an error for invalid item id$/ do
  screenshot_and_raise "Error" unless error_invalid_unlock_item_id?
end

Given /^I enter "([^\"]*)" as item name$/ do |name|
  item_name name
end

Given /^I open unlock offerwall for item "([^\"]*)"$/ do |itemId|
  launch_unlock_offerwall itemId
end

Given /^I request item status$/ do
  request_unlock_items
  sleep 1
end

Then /^I see the item description "([^\"]*)"$/ do |description|
  screenshot_and_raise "Item description is different" unless (get_item_name =~ /\A#{description}\Z/) != nil
  sleep 1
end
