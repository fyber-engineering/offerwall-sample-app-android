
def goto_banners
  if !banner_fragment? && main_activity?
    # performAction('press_button_with_text', 'Banners')
    press_button 'Banners'
  end
end

def banner_fragment?
  loaded_fragment == 'Banners'
  rescue
  false
end

def request_banner
  raise 'Banners fragment not loaded' unless banner_fragment?
  #raise "There's no valid credentials" unless valid_credentials?
  press_button 'Request Offer Banner'
end

def banner?
  raise 'Banners fragment not loaded' unless banner_fragment?
  #really bad performance wise
  #(performAction('inspect_current_dialog')["bonusInformation"][0] =~ /Request Offer Banner\<\/textViewText\>\<\/view\>\<view\>\<type\>WebView/) != nil
  query('webView').size > 0
end

def banner_error?
  banner_fragment?
  #really bad performance wise
  #(performAction('inspect_current_dialog')["bonusInformation"][0] =~ /Request Offer Banner\<\/textViewText\>\<\/view\>\<view\>\<type\>TextView/) != nil
  (query('textView', :text).grep /^Offer Banner/) != nil
end

def touch_banner
  raise "There's no banner yet" unless banner?
  performAction('click_on_view_by_id', 'banner_container')
end