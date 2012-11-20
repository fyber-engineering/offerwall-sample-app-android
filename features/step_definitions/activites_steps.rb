Then /^I see the main activity$/ do
  sleep 2
  raise_and_screenshot "Not on main activity" unless main_activity?
end

Given /^that I am on production$/ do
  sleep 2
  use_staging_urls false
end
