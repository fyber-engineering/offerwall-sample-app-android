And /^(?:that )I am on the rewarded actions page$/ do 
  goto_rewarded_actions
  sleep 0.2
end

Given /^I report completed action$/ do
  report_action
  sleep 1
end

When /^(?:that )I use empty action id$/ do
  action_id ''
end

When /^I report "([^\"]*)" action as completed$/ do |action_id|
  action_id action_id
  report_action
  sleep 1
end

Then /^I get an error for empty action id$/ do
  screenshot_and_raise "Error" unless error_empty_action_id?
end

Then /^I get an error for invalid action id$/ do
  screenshot_and_raise "Error" unless error_invalid_action_id?
end