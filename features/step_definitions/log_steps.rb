Then /^(?:T|t)he log shows the following:$/ do |table|
  goto_main_activity
  sleep (1.5)
  table.raw.each do |text|
    text = text.first
    screenshot_and_raise "No text found containing: #{text}" unless get_log_content.include? text
  	# res = query("view {accessibilityLabel == 'Log_TextView' and text CONTAINS '#{text}'}").empty?
    # screenshot_and_raise "No text found containing: #{text}" if res
  end
end

Then /^(?:T|t)he log ends with "([^\"]*)"$/ do |text|
  goto_main_activity
  sleep (1.5)
  screenshot_and_raise "No text found containing: #{text}" unless get_log_content.rstrip.end_with? text
end