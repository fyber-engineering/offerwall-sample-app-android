Then /^I dismiss the dialog$/ do
  sleep 1
  dismiss_dialog
  sleep 1
end

And /^I get an error for missing security token$/ do
  raise_and_screenshot "Error was not displayed" unless error_missing_security_token?
end

Then /^I get an error for invalid signature$/ do
  raise_and_screenshot "Error was not displayed" unless error_invalid_signature?
end


Then /^I get an error for invalid application id$/ do
  raise_and_screenshot "Error was not displayed" unless error_invalid_appid?
end