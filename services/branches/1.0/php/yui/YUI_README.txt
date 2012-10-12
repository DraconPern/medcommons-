1. This customized version of YUI has the tab view tabs colored gray.

To do this the sprite and the tabview.css are modified.

see tabview/assets/skins/sam/tabview.css <=== can't see where CSS is modified, not updated for 2.7.0

see the sprite in assets/skins/sam/sprite.png

     
2. Upgrade Source Files
 ==== vvvvvvvvv I did not do this for 2.7.0, I don't know why it was needed vvvvvvvvvvv  ====
|
|  To upgrade source files to reference a new version of YUI you can use sed to find old references and change the version:
|
|  grep -i 'yui/' *.* -l | xargs sed -i.bak 's/yui\/2.5.2/yui\/2.6.0/g'


3. Fix Relative URLs

Modify URL of arrow.png in button.css to absolute - change

  menu-button-arrow.png
to 
  /yui/2.7.0/button/assets/skins/sam/menu-button-arrow.png

Needs to be done in 2 places, including  /ct/yui/2.7.0/assets/skins/sam

ALSO:  Change references to sprite.png to absolute URLs:

cd /ct/yui/2.7.0/assets/skins/sam
$ for i in `grep -l  sprite.png *.css`; do echo $i;   sed -i.bak 's/sprite.png/\/yui\/2.7.0\/assets\/skins\/sam\/sprite.png/g' $i; done

