Given /^that I am user "([^\"]*)" with valid credentials for "([^\"]*)"$/ do |userId, appId|
  check_and_create_credentials userId, appId
  sleep 0.2
end

Given /^I use "([^\"]*)" as security token$/ do |securityToken|
  enter_text( "#{securityToken}", "security_token_field")
  push_credentials_button
  screenshot_and_raise "Credentials were not updated" unless valid_credentials? ".*", ".*", securityToken
end

Given /^that I am an unique user for app "([^\"]*)"$/ do |appId|
  userId = generate_unique_user
  puts "User -> #{userId}" 
  check_and_create_credentials userId, appId
  sleep 0.22
end

Given /^(?:that )I start the SDK$/ do
  push_credentials_button
end