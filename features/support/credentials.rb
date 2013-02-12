def check_and_create_credentials (user_id, app_id, security_token=nil)
  raise "Invalid app_id" unless not app_id.empty?
  unless valid_credentials? user_id,  app_id
    create_credentials user_id, app_id, security_token
    raise "Impossible to get valid credentials" unless valid_credentials? user_id,  app_id
  end
end

def create_credentials (user_id, app_id, security_token=nil)
  enter_text( app_id, 'app_id_field')
  enter_text( user_id, 'user_id_field')
  enter_text( security_token, 'security_token_field')
  push_credentials_button
end

def push_credentials_button
  touch("button marked:'Create new credentials'")
end

def valid_credentials? (user_id='.*', app_id='.*', security_token='.*')
  sleep 0.2
  user_id = '.*' if user_id.empty?
  credentialsInfo = performAction( 'get_view_property', 'credentials_info', 'text')['message']
  (credentialsInfo =~ /AppId - #{app_id}\nUserId - #{user_id}\nSecurityToken - #{security_token}/) != nil 
end

def security_token_set?
  credentialsInfo = query("textView id:'credentials_info'", :text).first
  unless credentialsInfo.nil? || credentialsInfo.empty?
    (credentialsInfo =~ /SecurityToken - N\/A/) == nil 
  else
    false
  end
end