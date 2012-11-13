Given /^I am able to launch the application$/ do
	element_exists("view")
	sleep(STEP_PAUSE)
end

Given /^I am user "([^\"]*)"$/ do |name|
	set_text("textField accessibilityLabel:'userIdTextField'", name)
	sleep(STEP_PAUSE)
end

Given /^that user "([^\"]*)" has a valid session$/ do |name|
	if ()
end