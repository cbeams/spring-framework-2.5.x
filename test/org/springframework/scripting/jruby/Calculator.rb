require 'java' 

include_class 'org.springframework.scripting.Calculator'

class RubyCalculator < Calculator

 def add(x, y)
    x + y
 end

end