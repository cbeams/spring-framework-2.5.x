require 'java'

include_class 'org.springframework.scripting.Messenger'

class RubyMessenger < Messenger

 def setMessage(message)
  @@message = message
 end

 def getMessage
  @@message
 end
end
