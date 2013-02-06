def press_button (button)
  raise 'No button provided' if button.empty? || button.nil?
  performAction 'press_button_with_text', button
end

def press_back_button
  performAction('scroll_up')
  touch("Button marked:'Back'")  
end

def use_staging_urls (staging=true)
  touch("CheckBox id:'use_staging_urls_checkbox'") if staging? ^ staging
end

def staging?
  check_main_activity
  query("CheckBox id:'use_staging_urls_checkbox'", :checked).first
end

def main_activity?
  performAction('get_activity_name')['message'] == 'SponsorpayAndroidTestAppActivity'
end

def check_main_activity
  raise 'Not on main activity screen' unless main_activity?
end

def loaded_fragment
  raise 'No fragment loaded at the moment' unless fragment?
  query("textView id:'fragmentTitle'", :text).first
end

def fragment?
  check_main_activity
  query("textView id:'fragmentTitle'").size > 0
end

def generate_unique_user
  "testuser-#{Time.now.to_i}"
end

def currency (currency=nil)
  enter_text currency, 'currency_name_field'
end

def check_offerwall
  raise 'Offerwall is not visible at the moment' unless offerwall_visible? || interstitial_visible?
end
