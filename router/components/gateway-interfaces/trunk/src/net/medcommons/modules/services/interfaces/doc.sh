#!/bin/bash
rm -rf javadoc
javadoc -private -d javadoc *.java
cat javadoc/net/medcommons/modules/services/interfaces/{DocumentService.html,TrackingService.html,HipaaService.html,TrackingReference.html,DocumentReference.html} > interfaces.html
echo
echo "done."
echo
