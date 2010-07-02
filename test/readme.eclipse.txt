This readme describes how to run the tests in this folder
using the Eclipse IDE.

1) Select "Run..." from the Run drop-down menu
2) Create a new "JUnit" launch configuration 
   - call it "Spring-tests" or some similar name
3) Check the option to "run all tests in the selected
   project, package or source folder".
4) Click search and select this folder (tests)
5) Click on the "Classpath" tab and then "User Entries"
6) Select "Add External Jars" and add tools.jar from
   your JDK.
7) Select "Advanced", then "Add folders". 
8) Add the "spring/target/test-classes" folder
9) "Apply" the changes
10) Run!

-- Adrian Colyer, December 2005.