This is a common-build system. If only one project is using it, then it generally
makes the most sense to put it in a directory like 'common-build' under the project
root, and make the 'static-repo' which is the base dir for most libraries, just be
the lib dir in the project. As soon as this is going to be shared by more than one
project (which is very handy, as projects can then have dependencies between them
too), then it makes sense to move this out to be parallel in the directory tree with
the projects using it, and move the static-repo to another parallel directory.

Projects using this need only a very minimal build.xml file. They may still override
and add to targets in the common-targets.xml, by using the dependency mechanism to
hook themselves in as needed. For example, to hook into the normal 'statics' target,
just do
  <target name="statics" depends="common-targets.statics">
this will insert the custom 'statics' target in the project's build.xml after the
the standard one, still running the standard one first. If there is a need to
insert before it, just depend on the 'pre' target instead:
  <target name="statics" depends="common-targets.statics-pre">
