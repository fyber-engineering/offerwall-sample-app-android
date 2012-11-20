Given /^that I am user "([^\"]*)" with valid credentials for "([^\"]*)"$/ do |userId, appId|
  check_and_create_credentials userId, appId
  sleep 2
end

Given /^I use "([^\"]*)" as security token$/ do |securityToken|
  enter_text( "#{securityToken}", "security_token_field")
  push_credentials_button
  raise_and_screenshot "Credentials were not updated" unless valid_credentials? ".*", ".*", securityToken
end