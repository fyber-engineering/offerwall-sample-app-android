Then /^I request coins$/ do
  request_coins
end


Then /^I get "([^\"]*)" coins$/ do |coins|
  raise_and_screenshot "Amount of coins is different" unless (get_alert_error_message =~ /Delta of Coins: #{coins}\n\n/ ) != nil
end

Then /^I get some coins$/ do
  m = get_alert_error_message.match /Delta of Coins: (\d+.?\d+)\n\n/
  raise_and_screenshot "Impossible to get coins" unless m != nil
  raise_and_screenshot "No coins available" unless  m[1].to_f > 0
end

Then /^I get no coins$/ do
  raise_and_screenshot "Amount of coins is different" unless (get_alert_error_message =~ /Delta of Coins: 0.0\n\n/ ) != nil
end

