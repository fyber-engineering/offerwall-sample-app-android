
#setting activity 
def goto_settings
  if !settings_page?
    performAction('scroll_up')
    performAction('press_image_button_number', '1')
    performAction('wait_for_screen', 'MainSettingsActivity', '15')
  end
end

def settings_page?
  performAction('get_activity_name')["message"] == "MainSettingsActivity"
end

#overriding url
def enter_overriding_url (url)
  check_settings_page
  raise "Invalid URL provided" unless !url.empty?
  enter_text(url, 'overriding_url_field')
end

def clear_overriding_url
  check_settings_page
  enter_text("", 'overriding_url_field')
end

def add_params
  check_settings_page
  performAction('press_button_with_text', 'params')
end

#custom variables
def clear_all_parameters
  check_settings_page
  performAction('press_button_with_text', 'Clear All')
end

def add_custom_parameters (key, value)
  check_settings_page
  raise "Invalid key provided" unless !key.empty?
  raise "Invalid value provided" unless !value.empty?
  enter_text(key, 'custom_key_field')
  enter_text(value, 'custom_value_field')
  performAction('press_button_with_text', 'Add')
  raise "An error happened, key/value pair was not added" unless pair_added? key, value
end

def pair_added? (key, value)
  check_settings_page
  raise "Invalid key provided" unless !key.empty?
  raise "Invalid value provided" unless !value.empty?
  pairs = performAction( 'get_view_property', 'key_values_list', 'text')["message"]
  (pairs =~ /#{key} = #{value}/) != nil  
end

#application data
def clear_app_data
  check_settings_page
  performAction('press_button_with_text', 'Clear Application Data')
end

#checkbox stuff
def keep_offerwall_open?
  checkbox_checked? "keep_offerwall_open_checkbox"
end

def set_keep_offerwall_open (open=true)
  set_checkbox( 'keep_offerwall_open_checkbox', 1 , open)
end

def simulate_no_phone_state_permission?
  checkbox_checked? "simulate_no_phone_state_permission"
end

def set_no_phone_state_permission (permission=false)
  set_checkbox( 'simulate_no_phone_state_permission', 2 , permission)
end

def simulate_no_wifi_state_permission?
  checkbox_checked? "simulate_no_wifi_state_permission"
end

def set_no_wifi_state_permission (permission=false)
  set_checkbox( 'simulate_no_wifi_state_permission', 3 , permission)
end

def simulate_invalid_android_id?
  checkbox_checked? "simulate_invalid_android_id"
end

def set_invalid_android_id (invalid=false)
  set_checkbox( 'simulate_invalid_android_id', 4 , invalid)
end

def simulate_no_hw_serial?
  checkbox_checked? "simulate_no_hw_serial_number"
end

def set_no_hw_serial (nohw=false)
  set_checkbox( 'simulate_no_hw_serial_number', 5 , nohw)
end


#helper methods
def set_checkbox (checkboxId, checkboxIndex, open=true)
  check_settings_page
  raise "You have to provide a checkbox id" unless !checkboxId.empty?
  raise "You have to provide a checkbox index" unless checkboxIndex != nil
  raise "Invalid parameter, must be a Boolean" unless !!open == open  
  if (checkbox_checked? checkboxId) ^ open
    performAction('toggle_numbered_checkbox', checkboxIndex)
  end
end

def checkbox_checked? (checkboxId)
  check_settings_page
  raise "You have to provide a checkbox id" unless !checkboxId.empty?
  performAction('get_view_property', checkboxId, 'checked')["message"].to_bool
end

def check_settings_page
  raise "Not on settings page" unless settings_page?
end

