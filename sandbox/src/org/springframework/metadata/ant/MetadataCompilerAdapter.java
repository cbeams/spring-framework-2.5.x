package org.springframework.metadata.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapterFactory;

/**
 * A first shot at implementing a compiler adapter so that compiling with
 * attributes very easy to invoke from within Ant.  This might also be
 * implemented using an an build listener when the javac task finishes. 
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * 
 */
public class MetadataCompilerAdapter implements CompilerAdapter {

    /**
     * A reference to the Javac task set by the Ant runtime.
     */
    private Javac _javac;

    /**
     * Set the Javac task. 
     * @see org.apache.tools.ant.taskdefs.compilers.CompilerAdapter#setJavac(org.apache.tools.ant.taskdefs.Javac)
     */
    public void setJavac(Javac javac) {
        _javac = javac;

    }

    /**
     * Run the normal javac task, then extract parameters from the javac task and run
     * the MetadataCompilerTask
     * @see org.apache.tools.ant.taskdefs.compilers.CompilerAdapter#execute()
     */
    public boolean execute() throws BuildException {

        //TODO mimic the decision logic to determine which 'original' compiler adapter to run....
        //if (JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_4) {
        //TODO support other compilers
        String compilerFactorySwitch = "javac1.4";

        //Need to pass in javac just so it can have access to logging facility.
        CompilerAdapter originalAdapter =
            CompilerAdapterFactory.getCompiler(compilerFactorySwitch, _javac);
        originalAdapter.setJavac(_javac);
        boolean javacOK = originalAdapter.execute();

        if (javacOK = false) {
            return false;
        } else {

            //At this point, javac has been called....

            MetadataCompilerTask aTask = new MetadataCompilerTask();
            String err = aTask.initializeMetadataTask(_javac);
            if (null != err) {
                throw new BuildException(err, _javac.getLocation());
            }
            aTask.execute();

            return true;
        }

    }

}
