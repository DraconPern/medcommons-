There are two ant build files for this project due to a classpath issue. 

Here's how to build this project and publish the ccrxmlbean.jar file - just run ant twice
1) ant -f build-first.xml
2) ant publish

==
build.xml contains two taskdefs that are not defined until the jar files are retrieved by ivy. So - build-first.xml retrieves the xmlbeans jar files via ivy; then
build.xml can be invoked.

