def scroll_until (button)
  finish = false
  until finish do
    begin
      finish = performAction('scroll_to', "css","#{button}")["success"]
    rescue
    end
  end
end

def touch_button (button)
  yield
  begin
    scroll_until "#{button}"
  rescue
  end
  performAction('touch', "css","#{button}")
end


