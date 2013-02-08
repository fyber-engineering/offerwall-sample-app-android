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

def goto_main_activity
  unless main_activity?
    #settings
    if settings_page?
      performAction('scroll_up')
      press_button 'Back'
    else
      raise 'Probably on SDK activity, canceling...'
    end
  end
end

def main_activity?
  performAction('get_activity_name')['message'] == 'SponsorpayAndroidTestAppActivity'
end

def check_main_activity
  raise 'Not on main activity screen' unless main_activity?
end

def loaded_fragment
  raise 'No fragment loaded at the moment' unless fragment?
  fragment_id = query("textView id:'fragmentTitle'", :text).first
  if fragment_id.nil?
    performAction 'scroll_down'
    fragment_id = query("textView id:'fragmentTitle'", :text).first
  end
  fragment_id
end

def fragment?
  check_main_activity
  query('textView', :id).include? "fragmentTitle"
  # query("textView id:'fragmentTitle'").size > 0
end


def currency (currency=nil)
  enter_text currency, 'currency_name_field'
end

def check_offerwall
  raise 'Offerwall is not visible at the moment' unless offerwall_visible? || interstitial_visible?
end
