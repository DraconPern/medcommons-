#################################################
#
# Copyright MedCommons Inc. 2005
#
# This Makefile builds the installer for the MedCommons CXP 
# Plugin for FireFox/Mozilla
#
# Author:   Simon Sadedin, MedCommons Inc.
#
################################################

VERSION=0.9.39

PLUGIN_FILES=$(shell find plugin -type f | grep -v '\.svn')

# Where FireFox is installed
FIREFOX_DIR=/cygdrive/c/Program\ Files/Mozilla\ Firefox/

# The name of the installer file
PLUGINFILE=mccxp.jar

# The name of the output file for the installer
INSTALLER=mccxp_$(VERSION).xpi

all:  installer

plugin: $(PLUGINFILE) 

version:
	sed -i.bak 's/".*"/"'$(VERSION)'"/g' plugin/content/mccxp/ver.dtd
	sed -i.bak 's/<em:version>.*<\/em:version>/<em:version>'$(VERSION)'<\/em:version>/g' install/install.rdf

installer: plugin $(INSTALLER)

svn: clean update installer
	svn add $(INSTALLER)
	svn propset svn:mime-type application/x-xpinstall $(INSTALLER)

test: installer
	$(FIREFOX_DIR)firefox "file://c:/mc/svn/ff/trunk/$(INSTALLER)"

update:
	@echo
	@echo "Checking for updates ...."
	@echo
	svn update .
	@echo

install_build:
	mkdir install_build
	mkdir install_build/chrome

$(INSTALLER): plugin install_build
	cp mccxp.jar install_build/chrome
	cp install/install.rdf install_build
	cd install_build; zip -r ../$(INSTALLER) *

$(PLUGINFILE): $(PLUGIN_FILES) version
	cd plugin; zip ../$(PLUGINFILE) `find content/mccxp -type f | grep -v '\.svn'` `find skin -type f | grep -v '\.svn'`

.PHONY: clean update version

clean:
	rm -rf $(PLUGINFILE) $(INSTALLER) install_build

