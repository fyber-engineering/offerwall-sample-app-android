Then /^I dismiss the dialog$/ do
  sleep 1
  dismiss_dialog
  sleep 1
end

And /^I get an error for missing security token$/ do
  sleep 2
  screenshot_and_raise "Error was not displayed" unless error_missing_security_token?
end

Then /^I get an error for invalid signature$/ do
  sleep 2
  screenshot_and_raise "Error was not displayed" unless error_invalid_signature?
end

Then /^I get an error for invalid application id$/ do
  sleep 2
  screenshot_and_raise "Error was not displayed" unless error_invalid_appid?
end

Then /^I get an error for missing application id$/ do
  sleep 2
  screenshot_and_raise "Error was not displayed" unless error_missing_appid?
end

Then /^I get an error for missing credentials$/ do
  sleep 2
  screenshot_and_raise "Error was not displayed" unless error_no_credentials?
end
