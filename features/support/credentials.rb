def check_and_create_credentials (userId, appId, securityToken=nil)
  raise "Invalid appId" unless not appId.empty?
  unless valid_credentials? userId,  appId
    create_credentials userId, appId, securityToken
    raise "Impossible to get valid credentials" unless valid_credentials? userId,  appId
  end
end

def create_credentials (userId, appId, securityToken=nil)
  enter_text( "#{appId}", "app_id_field")
  enter_text( "#{userId}", "user_id_field")
  enter_text( "#{securityToken}", "security_token_field")
  push_credentials_button
end

def push_credentials_button
  touch("button marked:'Create new credentials'")
end

def valid_credentials? (userId=".*", appId=".*", securityToken=".*")
  userId = ".*" if userId.empty?
  credentialsInfo = performAction( 'get_view_property', 'credentials_info', 'text')["message"]
  (credentialsInfo =~ /AppId - #{appId}\nUserId - #{userId}\nSecurityToken - #{securityToken}/) != nil 
end

def security_token_set?
  credentialsInfo = query("textView id:'credentials_info'", :text).first
  unless credentialsInfo.nil? || credentialsInfo.empty?
    (credentialsInfo =~ /SecurityToken - N\/A/) == nil 
  else
    false
  end
end