#!/bin/bash
cd /home/ci/build/console/
svn update .
# TTW - don't understand why we don't refresh entire console?
sudo svn --force export . /var/www/console
#sudo svn --force export templates /var/www/console/templates
#sudo svn --force export media /var/www/console/media
sudo /etc/init.d/httpd restart
