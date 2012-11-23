
def goto_banners
  if !banner_fragment? && main_activity?
    performAction('press_button_with_text', 'Banners')
  end
end

def banner_fragment?
  begin
    loaded_fragment == "Banners"
  rescue
    false
  end
end

def request_banner
  raise "Banners fragment not loaded" unless banner_fragment?
  #raise "There's no valid credentials" unless valid_credentials?
  performAction('press_button_with_text', 'Request Offer Banner')
end

def banner?
  banner_fragment?
  #really bad performance wise
  #(performAction('inspect_current_dialog')["bonusInformation"][0] =~ /Request Offer Banner\<\/textViewText\>\<\/view\>\<view\>\<type\>WebView/) != nil
  begin
    query("webView css:*")
    true
  rescue
    false
  end
end

def banner_error?
  banner_fragment?
  #really bad performance wise
  (performAction('inspect_current_dialog')["bonusInformation"][0] =~ /Request Offer Banner\<\/textViewText\>\<\/view\>\<view\>\<type\>TextView/) != nil
end

def touch_banner
  raise "There's no banner yet" unless banner?
  performAction("click_on_view_by_id", "banner_container")
end