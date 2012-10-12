#!groovy
/***************************************************
 * MedCommons Release Notes Script
 * 
 * This script examines the branch file(s) for the 
 * version(s) specified on the command line and
 * then compiles a consolidated release notes file
 * from Subversion showing changes between the two 
 * versions.  If a version file is specified but
 * does not exist then a new version file is 
 * generated based on the current HEAD revisions 
 * in Subversion.
 */

import java.text.SimpleDateFormat

println "============================================================================"
println "MedCommons Release Notes Script".toUpperCase().center(60," ")
println "============================================================================"
println ""

if(args.size() < 2) {
  println """
Usage:  releasenotes.groovy <version1> <version2>

Example:
        releasenotes.groovy 1.0.2 
"""
  System.exit(1);
}

def branch = args[1]
def oldbranch = args[0]

def repos = [ 
  router: "https://svn.medcommons.net/svn/router/branches",
  services: "https://svn.medcommons.net/svn/services/branches",
  ddl: "https://svn.medcommons.net/svn/router/components/dicomclient/branches"
]

def oldrevisions = new Properties()
if(!new File("${oldbranch}.txt").exists()) {
  println "Cannot find file ${oldbranch}.txt."
  System.exit(1)
}
oldrevisions.load(new FileInputStream("${oldbranch}.txt"))

def revisions = new Properties()
if(!new File("${branch}.txt").exists()) {
  println "\nUnable to find file ${branch}.txt.  Generating default."

  for(def repo in ["router","services","ddl"]) {
    def out = "svn info ${repos[repo]}/1.0/".execute().text
    def rev = out.readLines().grep{it.indexOf("Revision:")>=0}[0].split(":")[1]
    revisions[repo]=rev.trim()
  }
  revisions.save(new FileOutputStream("${branch}.txt"),"Auto Generated Branch Revision File")
  println "Saved new revision file ${branch}.txt" 
}
else
  revisions.load(new FileInputStream("${branch}.txt"))

def processed = ""
for(def repo in ["router","services","ddl"]) {
  def rev = revisions[repo].trim() 
  def oldrev = oldrevisions[repo].trim()

  println " Processing $repo @ $rev ".center(70,"#")

  def cmd = "svn log -r $oldrev:$rev ${repos[repo]}/1.0"

  println cmd

  def log = cmd.execute().text
  println log
  println ""

  processed += process_log(log) + "\n"
}

println "Consolidated Log".center(75,"=")
println processed
System.exit(0)

def process_log(def log) {

  def lines = []
  log.eachLine { ln -> 
      lines << ln
  }

  def out = []
  boolean skip = false
  for(int i=0; i<lines.size()-3;++i) {
      if(lines[i+3] =~ /Auto update of revision tag/) {
          i+=4
          continue
      }

      if(lines[i+1] =~ /^r[0-9]{4,5}/) {
        // println ":"+lines[i+1]
        i+=2
        while(!lines[i].startsWith("---")) {
          if(lines[i].trim() != "") {
            out << "  -  "+lines[i]
          }
          i++
        }
        i--
        continue
      }
  }
  return  out.join("\n")
}

