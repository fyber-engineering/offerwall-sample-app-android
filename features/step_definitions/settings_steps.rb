Given /^(?:that |)I am on the setting page$/ do
  goto_settings
end

And /^(?:that |)I use the following parameters:$/ do |table|
  check_settings_page
  sleep (1.5)
    # | key  | value  |
    # | pub0 | value0 |
  table.hashes.each do |pair|
    add_custom_parameters pair['key'], pair['value']
  end
end
