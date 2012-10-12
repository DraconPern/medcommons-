#!groovy
/**
 * A simple script to turn relative paths in some yui files into absolute ones
 */
def components = [
  'menu','datatable','paginator','tabview','container','button','autocomplete', 'resize', 'layout', 'logger'
]

def dir = new File('').absoluteFile.name
println "Replacing references to  ../../../../ in components " + components + " with /router/$dir"

def skins = ['sam','mc']
def ant = new AntBuilder()

for(def f in components) {
  ant.copy(todir: "$f/assets/skins/mc") {
    fileset(dir:"$f/assets/skins/sam")
  }
}

for(def skin in skins) {

  components.each {
    File skinDir = new File("$it/assets/skins/$skin/");
    skinDir.eachFileMatch(~/.*\.css/) { f ->
        println "Processing $f"
        f.text = f.text.replace("../../../../", "/router/$dir/")
    }
  }
}

def yuiDir = new File(".").canonicalFile.name

for(def c in components) {
    File skinDir = new File("$c/assets/skins/mc/");
    skinDir.eachFileMatch(~/.*\.css/) { f ->
        println "Processing $f CSS"
        f.text = f.text.replace("yui-skin-sam", "yui-skin-mc")
                       .replace("assets/skins/sam/", "assets/skins/mc/")    
                       .replaceAll(/\((split.*?.png)\)/,'('+yuiDir+'/assets/skins/mc/$1)')
    }
}
