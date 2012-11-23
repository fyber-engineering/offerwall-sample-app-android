Then /^I request coins$/ do
  request_coins
end


Then /^I get "([^\"]*)" coins$/ do |coins|
  screenshot_and_raise "Amount of coins is different" unless (get_alert_error_message =~ /Delta of Coins: #{coins}\n\n/ ) != nil
end

Then /^I get some coins$/ do
  m = get_alert_error_message.match /Delta of Coins: (\d+.?\d+)\n\n/
  screenshot_and_raise "Impossible to get coins" unless m != nil
  screenshot_and_raise "No coins available" unless  m[1].to_f > 0
end

Then /^I get no coins$/ do
  screenshot_and_raise "Amount of coins is different" unless (get_alert_error_message =~ /Delta of Coins: 0.0\n\n/ ) != nil
end

Then /^I have no transactions yet$/ do
  screenshot_and_raise "The user already had a transaction" unless (get_alert_error_message =~ /Returned Latest Transaction ID: NO_TRANSACTION/ ) != nil
end
