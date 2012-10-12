==============================================================
This file provides notes about how to upgrade to a new version
of YUI without losing MedCommons customizations
==============================================================

1. unzip the 'build' folder inside the YUI distro somewhere
2. copy all it's contents so that /build overlays /yui-<version>
3. there is a medcommons 'skin' that you will need to update
   a. cd yui-<version>
   b. Run the fix_rel_css.groovy script which will do a bunch of 
      things including copy customized sprites from /or/yui-customizations,
      fix relative CSS paths so they work with our base tags, etc.
      
