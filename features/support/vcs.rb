def request_coins
  raise "Not on main activity" unless main_activity?
  press_button "Request New Coins For User"
end
