
def goto_items
  if !items_fragment? && main_activity?
    press_button "Items"
  end
end

def items_fragment?
    loaded_fragment == "Items"
  rescue
    false
end

def request_unlock_items
  check_items 
  press_button "Request SponsorPay Unlock Items"
end

def launch_unlock_offerwall (itemId=nil)
  check_items
  enter_text itemId, "unlock_item_id_field"
  press_button "Launch Unlock Offer Wall"
end

def item_name (name)
  check_items
  enter_text name, "unlock_item_name_field"
end

def wait_for_unlock_offerwall
  if performAction('wait_for_screen', 'OfferWallActivity', '15')["success"]
    wait_for(30.to_f) {
      unlock_offerwall_visible?
    }
  end
end

def unlock_offerwall_visible?
  value = false
  if performAction('get_activity_name')["message"] == "OfferWallActivity"
    begin 
      value = query("webView css:*")[0]["nodeName"].size > 0
    rescue
    end 
  end
  value
end

def get_item_name
  check_unlock_offerwall
  query("webView css:header h1 strong")[0]["textContent"]
end

def no_offers?
  query("webView css:.no_offer").size > 0
end

def offers?
  !no_offers?
end

def unlock_touch_back_button
  #touch_button("#btn-back")
  # this way, also works from the all the web pages
  # privacy, support, help
  buttons = ["closebtn ui-link", "ui-link"]
  buttons.each do |b|
    if query("webView css:a[class=\"#{b}\"]").size > 0
      touch_button_unlock("a[class=\"#{b}\"]")
      break
    end
  end
end

#helper methods

def touch_button_unlock (button)
  touch_button(button) { check_unlock_offerwall }
end

def check_unlock_offerwall
  raise "Unlock Offerwall is not visible at the moment" unless unlock_offerwall_visible?
end

def check_items 
  #(securityToken=false)
  raise "Items fragment not loaded" unless items_fragment?
  #raise "There's no valid credentials" unless valid_credentials?
  #raise "There's no securityToken" unless !securityToken || security_token_set?
end
