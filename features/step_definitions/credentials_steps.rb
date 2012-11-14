Given /^that user "([^\"]*)" has valid credentials for "([^\"]*)"$/ do |userId, appId|
  if (! valid_credentials? userId,  appId)
    enter_text( "#{appId}", "app_id_field")
    enter_text( "#{userId}", "user_id_field")
    performAction('press_button_with_text', 'Create new credentials')
    if (! valid_credentials? userId,  appId)
      screenshot_and_raise "Impossible to get valid credentials"
    end  
  end
end

def valid_credentials? (userId, appId)
  credentialsInfo = performAction( 'get_view_property', 'credentials_info', 'text')["message"]
  (credentialsInfo =~ /AppId - #{appId}\nUserId - #{userId}/) != nil 
end