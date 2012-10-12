Name:		medcommons-tools
Version:	0.2.359
Release:	1
Summary:	MedCommons Tools

Group:		MedCommons
License:	MedCommons License

Source0:	%{name}-%{version}.tar.gz
BuildRoot:	%{_tmppath}/%{name}-%{version}
BuildArch:	noarch

%description
3rd party tools components of the MedCommons appliance.

%prep
rm -rf $RPM_BUILD_ROOT

%install
tar xzf $RPM_SOURCE_DIR/%{name}-%{version}.tar.gz -C %{_tmppath}

%clean
rm -rf $RPM_BUILD_ROOT

%pre

%post
if [ ! -f /etc/rsyslog.conf ] ; then
	ln -s /etc/rsyslog-appliance.conf /etc/rsyslog.conf
fi
/sbin/chkconfig syslog off
/sbin/service syslog stop
/sbin/chkconfig --add rsyslog
/sbin/chkconfig rsyslog on
/sbin/service rsyslog start
chmod +x /etc/init.d/rsyslog
chmod +x /usr/local/sbin/rsyslogd
chmod +x /usr/local/lib/rsyslog/*

%files
%defattr(-,root,root,-)

/usr/local/lib/rsyslog/lmnet.so
/usr/local/lib/rsyslog/lmtcpclt.la
/usr/local/lib/rsyslog/imklog.la
/usr/local/lib/rsyslog/lmnsd_ptcp.la
/usr/local/lib/rsyslog/imfile.la
/usr/local/lib/rsyslog/immark.la
/usr/local/lib/rsyslog/imtcp.so
/usr/local/lib/rsyslog/imuxsock.so
/usr/local/lib/rsyslog/imudp.so
/usr/local/lib/rsyslog/lmnsd_ptcp.so
/usr/local/lib/rsyslog/immark.so
/usr/local/lib/rsyslog/omtesting.so
/usr/local/lib/rsyslog/lmregexp.la
/usr/local/lib/rsyslog/imudp.la
/usr/local/lib/rsyslog/lmtcpsrv.la
/usr/local/lib/rsyslog/lmnet.la
/usr/local/lib/rsyslog/omtesting.la
/usr/local/lib/rsyslog/imuxsock.la
/usr/local/lib/rsyslog/imtcp.la
/usr/local/lib/rsyslog/lmnetstrms.la
/usr/local/lib/rsyslog/imfile.so
/usr/local/lib/rsyslog/lmstrmsrv.la
/usr/local/lib/rsyslog/imklog.so
/usr/local/lib/rsyslog/lmnetstrms.so
/usr/local/lib/rsyslog/lmtcpsrv.so
/usr/local/lib/rsyslog/lmstrmsrv.so
/usr/local/lib/rsyslog/lmregexp.so
/usr/local/lib/rsyslog/lmtcpclt.so
/usr/local/sbin/rsyslogd
/usr/local/share/man/man5/rsyslog.conf.5
/usr/local/share/man/man8/rsyslogd.8
/usr/local/share/doc/rsyslog/queueWorkerLogic.jpg
/usr/local/share/doc/rsyslog/rsconf1_gssforwardservicename.html
/usr/local/share/doc/rsyslog/man_rsyslogd.html
/usr/local/share/doc/rsyslog/rsyslog_conf_actions.html
/usr/local/share/doc/rsyslog/ipv6.html
/usr/local/share/doc/rsyslog/professional_support.html
/usr/local/share/doc/rsyslog/rsyslog-example.conf
/usr/local/share/doc/rsyslog/omsnmp.html
/usr/local/share/doc/rsyslog/Makefile
/usr/local/share/doc/rsyslog/direct_queue_rsyslog2.png
/usr/local/share/doc/rsyslog/Makefile.in
/usr/local/share/doc/rsyslog/ns_gtls.html
/usr/local/share/doc/rsyslog/rsyslog_conf_examples.html
/usr/local/share/doc/rsyslog/direct_queue_directq.png
/usr/local/share/doc/rsyslog/imuxsock.html
/usr/local/share/doc/rsyslog/tls_cert_ca.jpg
/usr/local/share/doc/rsyslog/version_naming.html
/usr/local/share/doc/rsyslog/install.html
/usr/local/share/doc/rsyslog/debug.html
/usr/local/share/doc/rsyslog/rsyslog_conf_templates.html
/usr/local/share/doc/rsyslog/tls_cert_summary.html
/usr/local/share/doc/rsyslog/bugs.html
/usr/local/share/doc/rsyslog/build_from_repo.html
/usr/local/share/doc/rsyslog/v3compatibility.html
/usr/local/share/doc/rsyslog/contributors.html
/usr/local/share/doc/rsyslog/gssapi.png
/usr/local/share/doc/rsyslog/rsconf1_allowedsender.html
/usr/local/share/doc/rsyslog/rsyslog_mysql.html
/usr/local/share/doc/rsyslog/tls_cert_ca.html
/usr/local/share/doc/rsyslog/generic_design.html
/usr/local/share/doc/rsyslog/rsconf1_generateconfiggraph.html
/usr/local/share/doc/rsyslog/rsconf1_debugprintmodulelist.html
/usr/local/share/doc/rsyslog/rsconf1_droptrailinglfonreception.html
/usr/local/share/doc/rsyslog/gssapi.html
/usr/local/share/doc/rsyslog/direct_queue2.png
/usr/local/share/doc/rsyslog/Makefile.am
/usr/local/share/doc/rsyslog/rsconf1_dirowner.html
/usr/local/share/doc/rsyslog/rsconf1_escapecontrolcharactersonreceive.html
/usr/local/share/doc/rsyslog/rsconf1_debugprinttemplatelist.html
/usr/local/share/doc/rsyslog/rsyslog-vers.png
/usr/local/share/doc/rsyslog/rsyslog_ng_comparison.html
/usr/local/share/doc/rsyslog/imgssapi.html
/usr/local/share/doc/rsyslog/rsconf1_failonchownfailure.html
/usr/local/share/doc/rsyslog/rsyslog_conf_modules.html
/usr/local/share/doc/rsyslog/direct_queue_rsyslog.png
/usr/local/share/doc/rsyslog/rsyslog_php_syslog_ng.html
/usr/local/share/doc/rsyslog/tls_cert_errmsgs.html
/usr/local/share/doc/rsyslog/licensing.html
/usr/local/share/doc/rsyslog/ns_ptcp.html
/usr/local/share/doc/rsyslog/tls_cert_client.html
/usr/local/share/doc/rsyslog/direct_queue0.png
/usr/local/share/doc/rsyslog/dev_queue.html
/usr/local/share/doc/rsyslog/rsconf1_markmessageperiod.html
/usr/local/share/doc/rsyslog/syslog_protocol.html
/usr/local/share/doc/rsyslog/modules.html
/usr/local/share/doc/rsyslog/rsconf1_debugprintcfsyslinehandlerlist.html
/usr/local/share/doc/rsyslog/omoracle.html
/usr/local/share/doc/rsyslog/rainerscript.html
/usr/local/share/doc/rsyslog/im3195.html
/usr/local/share/doc/rsyslog/rsyslog_reliable_forwarding.html
/usr/local/share/doc/rsyslog/tls_cert.jpg
/usr/local/share/doc/rsyslog/queue_analogy_tv.png
/usr/local/share/doc/rsyslog/ommail.html
/usr/local/share/doc/rsyslog/rsyslog_confgraph_complex.png
/usr/local/share/doc/rsyslog/rsconf1_filegroup.html
/usr/local/share/doc/rsyslog/rsconf1_actionresumeinterval.html
/usr/local/share/doc/rsyslog/omlibdbi.html
/usr/local/share/doc/rsyslog/direct_queue3.png
/usr/local/share/doc/rsyslog/rsconf1_dircreatemode.html
/usr/local/share/doc/rsyslog/queues.html
/usr/local/share/doc/rsyslog/rsconf1_controlcharacterescapeprefix.html
/usr/local/share/doc/rsyslog/rsconf1_moddir.html
/usr/local/share/doc/rsyslog/rsconf1_resetconfigvariables.html
/usr/local/share/doc/rsyslog/imrelp.html
/usr/local/share/doc/rsyslog/dataflow.png
/usr/local/share/doc/rsyslog/rscript_abnf.html
/usr/local/share/doc/rsyslog/rsconf1_modload.html
/usr/local/share/doc/rsyslog/rsconf1_gsslistenservicename.html
/usr/local/share/doc/rsyslog/rsyslog_packages.html
/usr/local/share/doc/rsyslog/rsconf1_actionexeconlywhenpreviousissuspended.html
/usr/local/share/doc/rsyslog/ommysql.html
/usr/local/share/doc/rsyslog/syslog_parsing.html
/usr/local/share/doc/rsyslog/how2help.html
/usr/local/share/doc/rsyslog/imklog.html
/usr/local/share/doc/rsyslog/rsconf1_mainmsgqueuesize.html
/usr/local/share/doc/rsyslog/features.html
/usr/local/share/doc/rsyslog/rsconf1_umask.html
/usr/local/share/doc/rsyslog/direct_queue1.png
/usr/local/share/doc/rsyslog/imtcp.html
/usr/local/share/doc/rsyslog/rsyslog_conf_filter.html
/usr/local/share/doc/rsyslog/rsyslog_conf_global.html
/usr/local/share/doc/rsyslog/property_replacer.html
/usr/local/share/doc/rsyslog/droppriv.html
/usr/local/share/doc/rsyslog/tls_cert_machine.html
/usr/local/share/doc/rsyslog/rsyslog_tls.html
/usr/local/share/doc/rsyslog/rsyslog_stunnel.html
/usr/local/share/doc/rsyslog/rsyslog_conf_output.html
/usr/local/share/doc/rsyslog/expression.html
/usr/local/share/doc/rsyslog/netstream.html
/usr/local/share/doc/rsyslog/src/classes.dia
/usr/local/share/doc/rsyslog/src/queueWorkerLogic.dia
/usr/local/share/doc/rsyslog/src/tls_cert.dia
/usr/local/share/doc/rsyslog/rsconf1_dropmsgswithmaliciousdnsptrrecords.html
/usr/local/share/doc/rsyslog/rsyslog_conf.html
/usr/local/share/doc/rsyslog/rsconf1_gssmode.html
/usr/local/share/doc/rsyslog/tls_cert_udp_relay.html
/usr/local/share/doc/rsyslog/tls_cert_100.jpg
/usr/local/share/doc/rsyslog/rsyslog_recording_pri.html
/usr/local/share/doc/rsyslog/troubleshoot.html
/usr/local/share/doc/rsyslog/rsyslog_high_database_rate.html
/usr/local/share/doc/rsyslog/rsconf1_filecreatemode.html
/usr/local/share/doc/rsyslog/queues_analogy.html
/usr/local/share/doc/rsyslog/rsconf1_dirgroup.html
/usr/local/share/doc/rsyslog/imfile.html
/usr/local/share/doc/rsyslog/log_rotation_fix_size.html
/usr/local/share/doc/rsyslog/rsyslog_secure_tls.html
/usr/local/share/doc/rsyslog/omrelp.html
/usr/local/share/doc/rsyslog/rsconf1_fileowner.html
/usr/local/share/doc/rsyslog/tls_cert_scenario.html
/usr/local/share/doc/rsyslog/rsconf1_dynafilecachesize.html
/usr/local/share/doc/rsyslog/index.html
/usr/local/share/doc/rsyslog/rsconf1_repeatedmsgreduction.html
/usr/local/share/doc/rsyslog/rsyslog_confgraph_std.png
/usr/local/share/doc/rsyslog/history.html
/usr/local/share/doc/rsyslog/rsconf1_includeconfig.html
/usr/local/share/doc/rsyslog/queueWorkerLogic_small.jpg
/usr/local/share/doc/rsyslog/manual.html
/usr/local/share/doc/rsyslog/rsyslog_conf_nomatch.html
/usr/local/share/doc/rsyslog/tls_cert_server.html
/etc/rsyslog-appliance.conf
/etc/rsyslog-collector.conf
/etc/rc.d/init.d/rsyslog
/etc/logrotate.d/rsyslog
/etc/sysconfig/rsyslog

%changelog
