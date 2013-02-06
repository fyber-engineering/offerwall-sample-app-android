def scroll_until (element)
  raise "Cannot find element:#{element} on the webView" unless query("webView css:'#{element}'").size > 0
  finish = false
  try = 1
  until finish do
    raise 'Too many retries' unless try < 11
    # begin
    finish = performAction('scroll_to', 'css', element)['success']
    # rescue
    # end
    unless finish
      try+=1
      sleep 1
    end
  end
end

def touch_button (button)
  yield
  scroll_until button
  touch("webView css:'#{button}'")
end

def bad_request?
    query("webView css:'#badrequest'").size > 0
end

def get_currency
  yield if block_given?
  begin
    query("webView css:'.currencyType'")[0]["textContent"]
  rescue
    false
  end
end