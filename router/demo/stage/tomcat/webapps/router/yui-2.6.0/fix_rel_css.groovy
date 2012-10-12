#!groovy
/**
 * A simple script to turn relative paths in some yui files into absolute ones
 */
def files = ['datatable/assets/skins/sam/datatable.css', 'paginator/assets/skins/sam/paginator.css']
files.each {
  File f = new File(it);
  f.text = f.text.replace("../../../../", "/router/yui-2.6.0/")
}
