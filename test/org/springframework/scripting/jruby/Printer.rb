require 'java'

include_class 'org.springframework.scripting.jruby.Printer'

class RubyPrinter < Printer

	def print(obj)
	   puts obj.getContent
	end
end