/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.autobuilds.ant;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * HsqlShutdown
 * 
 * Custom ANT task to perform a clean shutdown of an hsql db instance.  Only
 * handles HSQL Server mode.
 * 
 * @author Darren Davison
 */
public class HsqlShutdown extends Task {

	
	// task attribs & defaults
	private String url = "jdbc:hsqldb:hsql://localhost:9001";
	private String user = "sa";
	private String password = "";
	private boolean compact = false; 
	 
	
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        super.execute();
        
		Connection con = null;
		Statement stmt = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
            stmt.execute("SHUTDOWN" + (compact ? " COMPACT" : ""));
            
        } catch (ClassNotFoundException e) {
            throw new BuildException("Failed to load HSQL driver - ensure hsqldb.jar is on the classpath");
            
        } catch (SQLException e) {
            throw new BuildException("Failed to connect to HSQL server instance: " + e.getMessage());
        }
        
        finally {
        	try {
        		if (stmt != null) stmt.close();
				if (con != null) con.close();
        	} catch (SQLException sqle_) {}
        }       	
        
    }

    /**
     * @see org.apache.tools.ant.Task#getDescription()
     */
    public String getDescription() {
        return "Shutdown a running hsqldb instance cleanly";
    }
    
    /**
     * @param b
     */
    public void setCompact(boolean b) {
        compact = b;
    }

    /**
     * @param string
     */
    public void setPassword(String string) {
        password = string;
    }

    /**
     * @param string
     */
    public void setUser(String string) {
        user = string;
    }

    /**
     * @param string
     */
    public void setUrl(String string) {
        url = string;
    }

}
