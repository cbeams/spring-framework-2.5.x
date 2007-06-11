require 'java'

class RubyMessenger
	include org.springframework.scripting.Messenger

	def setMessage(message)
		@@message = message
	end

	def getMessage
		@@message
	end
end
