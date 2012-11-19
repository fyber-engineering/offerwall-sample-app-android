Then /^I see the main activity$/ do
  sleep 2
  main_activity? == true
end

Given /^that I am on production$/ do
  sleep 2
  use_staging_urls false
end