folder containing instructions for BND tool when generating the OSGi manifests.
The definitions should have the .bar extension and have the same name as the artifact
name as the jar it has to decorate.

Commons pitfalls:
* spring.bnnd should include all 'special' definitions included in other jars
* the JTA libraries included in the JDK are incomplete - we have to specifically request for one with version 1.0.1 just in case
* asm is repacked internally and should be marked as an internal package
* aopalliance is bundled inside spring.jar and should be exported

Double check manifest:
* It is easy to make mistakes so *always* double check the artifact by using bnd print to
make sure everything is packed properly. Otherwise the mistake can get into the release
and cause considerably more trouble and it should.