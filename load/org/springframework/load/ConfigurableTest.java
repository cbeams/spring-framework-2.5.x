package org.springframework.load;

/**
 * 
 * @author Rod Johnson
 * @since 04-Dec-02
 */
public interface ConfigurableTest {
	
	/**
	 * Set the fixture object shared by all Test instances.
	 * Not all tests will require this.
	 */
	void setFixture(Object context) ;
	
	Object getFixture();
    
    long getMaxPause();
    void setMaxPause(long p);
    
    void setPasses(int count);
    int getPasses();

}
