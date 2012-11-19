Given /^that I am user "([^\"]*)" with valid credentials for "([^\"]*)"$/ do |userId, appId|
  check_and_create_credentials userId, appId
end
