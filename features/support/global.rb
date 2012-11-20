def press_button (button)
  raise "No button provided" unless !button.empty?
  performAction('press_button_with_text', button)
end

def press_back_button
  performAction('scroll_up')
  performAction('press_button_with_text','Back');
end

def use_staging_urls (staging=true)
  if staging? ^ staging
    performAction('toggle_numbered_checkbox', '1')
  end
end

def staging?
  check_main_activity
  performAction('get_view_property', 'use_staging_urls_checkbox', 'checked')["message"].to_bool
end

def main_activity?
  performAction('get_activity_name')["message"] == "SponsorpayAndroidTestAppActivity"
end

def check_main_activity
  raise "Not on main activity screen" unless main_activity?
end

def loaded_fragment
  check_main_activity
  raise "No fragment loaded at the moment" unless fragment?
  performAction('get_view_property', 'fragmentTitle', 'text')["message"]
end

def fragment?
  check_main_activity
  begin
    performAction('has_view', 'fragmentTitle')["success"]
  rescue
    false
  end
end
