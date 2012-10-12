==============================================================
This file provides notes about how to upgrade to a new version
of YUI without losing MedCommons customizations
==============================================================

1. unzip the 'build' folder inside the YUI distro somewhere
2. copy all it's contents so that /build overlays /yui
3. there is a medcommons 'skin' that you will need to update
   a. copy everything from menu/assets/skins/sam/ to yui/menu/assets/skins/mc
   b. edit the two css files -  menu.css, menu-skin.css and replace all
      references to yui-skin-sam with yui-skin-mc, and replace
      references to sam/sprite.png with mc/sprite.png
   c. go to yui/assets/skins/mc
   d. restore the 'sprite.png' to be the medcommons one.  Note it's possible
      the new YUI release updated the sprite.  If thats the case you might need
      to open both and copy the changed menu background into the new sprite and
      use the new one (yuck).
4. Run the fix_rel_css.groovy script which will turn some relative urls into
   absolute ones. This is necessary because they break when served packed
   with other scripts.
