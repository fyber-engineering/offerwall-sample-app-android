def alert_dialog?
    performAction('has_view', "android:id/alertTitle")["success"]
  rescue
    false
end

def get_alert_error_message
  raise "There's no alert dialog" unless alert_dialog?
  performAction('get_view_property', 'android:id/message', 'text')["message"]
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