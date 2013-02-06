def alert_dialog?
    query("dialogTitle id:'alertTitle'").size > 0
end

def get_alert_error_message
  raise "There's no alert dialog" unless alert_dialog?
  #performAction('get_view_property', 'android:id/message', 'text')["message"]
  query("textView id:'message'", :text).first
end

def dismiss_dialog
  raise "There's no alert dialog" unless alert_dialog?
  performAction('go_back')
  raise "Error while dismissing dialog" unless not alert_dialog?
end

def error_missing_security_token?
  (get_alert_error_message =~ /Security token/) != nil
end 

def error_invalid_signature?
  (get_alert_error_message =~ /ERROR_INVALID_SIGNATURE/) != nil
end

def error_invalid_appid?
  (get_alert_error_message =~ /ERROR_INVALID_APPID/) != nil
end

def error_missing_appid?
  (get_alert_error_message =~ /no valid App ID has been provided/) != nil
end

def error_invalid_unlock_item_id?
  (get_alert_error_message =~ /Item ID is not valid/) != nil
end

def error_no_credentials?
  (get_alert_error_message =~ /No credentials object was created yet/) != nil
end

def error_empty_action_id?
  (get_alert_error_message =~ /An ID cannot be null or empty/) != nil
end

def error_invalid_action_id?
  (get_alert_error_message =~ /Action ID is not valid/) != nil
end