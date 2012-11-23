And /^that I am on the banners page$/ do 
  goto_banners
  sleep 1
end

When /^I request a banner$/ do
  request_banner
  sleep 2
end

Then /^I see a banner$/ do
  screenshot_and_raise "No banner available" unless banner?
end

Then /^I see an error$/ do
  screenshot_and_raise "No error displayed" unless banner_error?
end

Then /^I touch the banner$/ do
  touch_banner
end