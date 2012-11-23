def goto_interstitial
  if !items_interstitial? && main_activity?
    press_button "Interstitial"
  end
end

def items_interstitial?
    loaded_fragment == "Interstitial"
  rescue
    false
end

def wait_for_interstitial
  if performAction('wait_for_screen', 'InterstitialActivity', '30')["success"]
    wait_for(30.to_f) {
      sleep 1
      interstitial_visible?
    }
  end
end

def interstitial_visible?
  value = false
  if interstitial?
    begin
      value = query("webView css:*")[0]["nodeName"].size > 0
    rescue
    end
  end
  value
end

def interstitial?
  performAction('get_activity_name')["message"] == "InterstitialActivity"
end

def close_interstitial
  touch_button ("a[href=\"sponsorpay://exit?status=1\"]") {interstitial_visible?}
end

def touch_download
  touch_button ("a[rel=\"external\"]") {interstitial_visible?}
end

def touch_more_apps
  touch_button ("#more") {interstitial_visible?}
end

def launch_interstitial
  check_interstitial_fragment
  press_button "Launch Interstitial"
end


def check_interstitial_fragment
  raise "Interstitial fragment not loaded" unless items_interstitial?
end