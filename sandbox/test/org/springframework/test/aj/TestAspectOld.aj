package org.springframework.test.aj;

public aspect TestAspect {

    before(): target(AdvicedHashCodeTests) && execution(void testCallHashCode()) {
         HashCodeMonitor.increment();
    }


}