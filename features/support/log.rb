def get_log_content
  goto_main_activity
  performAction 'scroll_up'
  query("TextView id:'log_text_view'", :text).first
end

# def log_content_contains? (text)
#   goto_main_activity
#   performAction 'scroll_up'
#   query("view {id == 'log_text_view' and text CONTAINS '#{text}'}")
# end