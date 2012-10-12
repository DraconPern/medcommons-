Organization of /probe

this is not all worked out, but the mobile version is 0.215, the other probes are 0.65 or so


There are a few separate useful executables in this project:

/www   contains test rigs and menus to install in www.medcommons.net


/ws     contains RESTful web services that run wherever installed


plain web pages and php , css, and /js folders form the top level probe displays


/collateral	has useful images

/custom	 needs a 60*60logo named logo60by60.png - go make one


/m has the mobile web site - it is produced by running uiSOXXX.dcproj and deploying
               to a local scratch directory and then copying into /m
               
/sintaxi-phonegap-etc - has a complete xCode and android development kit embedded within
				the /m scratch directory needs to get moved in,
				the actual server name needs to get patched into index.html
				Clean Build Targets
				find the PhoneGap.xcodeproj file and build and go
				if you don't have an iPhone wired up it will run in the simulator
				
				
				
Have fun



