Given /^that I am user "([^\"]*)" with valid credentials for "([^\"]*)"$/ do |userId, appId|
  check_and_create_credentials userId, appId
  sleep 0.2
end

Given /^(?:that |)I have valid credentials for "([^\"]*)"$/ do |appid|
  macro %Q|that I am user "#{random_user}" with valid credentials for "#{appid}"|
end

Given /^that I am user "([^\"]*)" with valid credentials$/ do |userid|
  macro %Q|that I am user "#{userid}" with valid credentials for "#{app_data[:appid]}"| 
end

Given /^I use "([^\"]*)" as security token$/ do |securityToken|
  enter_text securityToken, 'security_token_field'
  push_credentials_button
  screenshot_and_raise 'Credentials were not updated' unless valid_credentials? ".*", ".*", securityToken
end

Given /^that I am an unique user for app "([^\"]*)"$/ do |appid|
  userid = generate_unique_user
  puts "User -> #{userid}" 
  check_and_create_credentials userid, appid
  sleep 0.3
end

Given /^that I am an unique user with credentials$/ do
  macro %Q|that I am an unique user for app "#{app_data[:appid]}"|
end

Given /^(?:that |)I start the SDK$/ do
  push_credentials_button
end

Given /^(?:that |)I am a valid user with credentials$/ do 
  macro %Q|that I am user "#{random_user}" with valid credentials for "#{app_data[:appid]}"| 
end

Given /^(?:that |)I have correct security token$/ do
  macro %Q|I use "#{app_data[:token]}" as security token| 
end

Given /^(?:that |)I am a valid user with credentials for invalid app$/ do 
  macro %Q|that I am user "#{random_user}" with valid credentials for "#{app_data[:appid]}z"| 
end