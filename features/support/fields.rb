def enter_text (text, fieldId)
	performAction('clear_id_field', fieldId)
	performAction('enter_text_into_id_field', "#{text}", fieldId)
end