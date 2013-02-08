def goto_rewarded_actions
  if !rewarded_actions? && main_activity?
    # performAction('press_button_with_text', 'Banners')
    press_button 'Actions'
  end
end

def rewarded_actions?
  loaded_fragment == 'Actions'
  rescue
  false
end

def action_id (id)
  check_rewarded_actions
  enter_text id, 'action_id_field'
end

def report_action
  check_rewarded_actions
  press_button 'Report Action'
end

def check_rewarded_actions 
  raise 'Rewarded Actions fragment not loaded' unless rewarded_actions?
end

