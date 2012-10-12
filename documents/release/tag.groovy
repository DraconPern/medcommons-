#!/bin/groovy
/***************************************************
 * MedCommons Release Tagging Script
 * 
 * This script examines the branch file for the 
 * version specified on the command line and
 * then creates a tag for each branch revision.
 */

import java.text.SimpleDateFormat

println "============================================================================"
println "MedCommons Release Notes Script".toUpperCase().center(60," ")
println "============================================================================"
println ""

if(args.size() < 1) {
  println """
Usage:  tag.groovy <version>

Example:
        tag.groovy 1.0.2 
"""
  System.exit(1);
}

def branch = args[0]

def repos = [ 
  router: "https://svn.medcommons.net/svn/router/branches",
  services: "https://svn.medcommons.net/svn/services/branches",
  ddl: "https://svn.medcommons.net/svn/router/components/dicomclient/branches"
]

def revs = new Properties()
if(!new File("${branch}.txt").exists()) {
  println "Cannot find file ${branch}.txt."
  System.exit(1)
}


revs.load(new FileInputStream("${branch}.txt"))

for(def repo in ["router","services","ddl"]) {
  def rev = revs[repo].trim() 

  println " Processing $repo @ $rev ".center(70,"#")

  def cmd = "svn -m 'creating tag for version $branch' cp ${repos[repo]}/1.0 ${repos[repo]}/$branch"

  println ""
  println cmd

  println cmd.execute().text

  println ""
}

