Given /^that I am on the interstitial page$/ do 
  goto_interstitial
  sleep 1
end

Given /^that I launch the Interstitial$/ do 
  launch_interstitial
  sleep 2
end

And /^I wait for the Interstitial to become visible$/ do
  wait_for_interstitial
  sleep 4
end


Then /^I close the Interstitial$/ do
  close_interstitial
  sleep 0.5
end

Then /^I click on more apps$/ do
  touch_more_apps
  sleep 2
end

Then /^I click on the download button$/ do
  touch_download
  sleep 2
end
