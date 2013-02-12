def users
  ['tester_is_da_best', 'iLOVEtesting','testing_is_FUN','having_SOMUCH_fun','calabashing_is_cool','testsRdaBEST']
end

def random_user
  users.sample
end

def generate_unique_user
  "testuser-#{Time.now.to_i}"
end

def app_data
  case ENV['DATA']
  when 'actions'
    # rewarded actions
    {appid:'10876', token:'1825a7c9ccd470f5ed7446c99d0a8d24'}
  when 'mbe'
    # mbe tests
    {appid:'10870', token:'2255bd4ea73ff21177da978e87333448'}
  when 'dev'
    {appid:'1246', token:'12345678'}
  else
    #rewarded installs
    {appid:'10873', token:'ae047bf1ecd814d4deea0fc89eee836d'}
  end
end
