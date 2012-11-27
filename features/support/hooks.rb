After do |scenario|
  kill_application
  clear_application_data
end

Calabash::Android::Operations

module Calabash 
  module Android
    module Operations

      def kill_application()
        default_device.kill_application(ENV["PACKAGE_NAME"])
      end

      def clear_application_data()
        default_device.clear_application_data(ENV["PACKAGE_NAME"])
      end

      class Device

        def kill_application(package_name)
          cmd = "#{default_device.adb_command} shell ps | grep #{package_name} | awk '{print $2}' | xargs #{default_device.adb_command} shell kill"
          log "Killing application : #{package_name}"
          `#{cmd}`
        end

        def clear_application_data(package_name)
          path =  "/data/data/#{package_name}"
          list = "#{default_device.adb_command} shell ls #{path}"
          
          if !`#{list}`.empty?
            cmd ="#{default_device.adb_command} shell rm -R #{path}/*"
            log "Clearing application data: #{package_name} -> #{cmd}"
            result = `#{cmd}`
            if result.empty?
              log "Success"
            else
              log "#Failure"
              log "'#{cmd}' said:"
              log result.strip
              raise "Could not clear application data -> #{package_name}: #{result.strip}"
            end
          else
            log "no files to delete"
          end
        end
      end

    end
  end
end
