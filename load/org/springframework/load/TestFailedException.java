package org.springframework.load;



import org.springframework.core.NestedCheckedException;

public class TestFailedException extends NestedCheckedException {

   public TestFailedException( String message ) {
      super( message );
   }


   public TestFailedException( String message, Throwable t) {
      super( message, t);
   }

}
