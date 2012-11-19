def check_and_create_credentials (userId, appId, securityToken=nil)
  raise "Invalid appId" unless not appId.empty?
  if (! valid_credentials? userId,  appId)
    create_credentials userId, appId, securityToken
    if (! valid_credentials? userId,  appId)
      screenshot_and_raise "Impossible to get valid credentials"
    end  
  end
end

def create_credentials (userId, appId, securityToken=nil)
  enter_text( "#{appId}", "app_id_field")
  enter_text( "#{userId}", "user_id_field")
  enter_text( "#{securityToken}", "security_token_field")
  performAction('press_button_with_text', 'Create new credentials')
end

def valid_credentials? (userId=".*", appId=".*", securityToken=".*")
  credentialsInfo = performAction( 'get_view_property', 'credentials_info', 'text')["message"]
  (credentialsInfo =~ /AppId - #{appId}\nUserId - #{userId}\nSecurityToken - #{securityToken}/) != nil 
end

def security_token_set?
  credentialsInfo = performAction( 'get_view_property', 'credentials_info', 'text')["message"]
  (credentialsInfo =~ /SecurityToken - N\/A/) == nil 
end

