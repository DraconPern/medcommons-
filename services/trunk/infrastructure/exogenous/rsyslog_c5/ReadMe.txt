The rsyslog-4.4.2 folder is http://freshmeat.net/projects/rsyslog/ (stable version of 102309) source buil
t for fedora 7/i386. The mc_f7_rsyslog folder and mc_f7_rsyslog.tar tarball are the distribution objects
relative to root:

	#> cd /; tar xvf <path to>mc_f7_rsyslog.tar
	#> /sbin/chkconfig syslog off
	#> /etc/init.d/syslog off
	#> /sbin/chkconfig --add rsyslog
	#> ln -s /etc/rsyslog-appliance.conf /etc/rsyslog.conf
	#> mkdir /etc/logrotate.d-orig; mv /etc/logrotate.d/syslog /etc/logrotate.d-orig
	#> /etc/init.d/rsyslog on
 
And... for the x64/CentOS 5 appliance using the standard rsyslog pkg, remove everything but the config file(s) which are our own/crafted for our network to remote to hot.mc.n (nVa 110311@1831).
