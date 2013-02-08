Then /^I see the main activity$/ do
  sleep 0.5
  screenshot_and_raise "Not on main activity" unless main_activity?
end

Given /^(?:that |)I am on production$/ do
  sleep 0.2
  use_staging_urls false
end

Given /^(?:that |)I am on staging$/ do
  sleep 0.2
  use_staging_urls true
end

Given /^that custom currency is set to "([^\"]*)"$/ do |currencyName|
  currency currencyName
end

Then /^I see a "Bad request!" page$/ do
  screenshot_and_raise "Page was not found" unless bad_request?
end

Then /^I see a "Cookies required" page$/ do
  screenshot_and_raise "Page was not found" unless cookies_required?
end

Then /^the currency is "([^\"]*)"$/ do |currency|
  screenshot_and_raise "Currency is not #{currency}" unless get_currency == currency
end
