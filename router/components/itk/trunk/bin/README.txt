After cmake is installed on your system, invoke 
ccmake ../src/native
This generates an error (the ITK_DIR is not set).
enter 'e' to exit help
Set the value of ITK_DIR. Then hit 'g' to generate makefiles

Then invoke
make
and the executables will build.
