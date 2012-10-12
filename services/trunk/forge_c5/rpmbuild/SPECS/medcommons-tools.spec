Name:		medcommons-tools
Version:	0.2.376
Release:	1
Summary:	MedCommons Tools

Group:		MedCommons
License:	MedCommons License

Source0:	%{name}-%{version}.tar.gz
BuildRoot:	%{_tmppath}/%{name}-%{version}
BuildArch:	noarch

Requires:	rsyslog

%description
3rd party tools components of the MedCommons appliance.

%prep
rm -rf $RPM_BUILD_ROOT

%install
tar xzf $RPM_SOURCE_DIR/%{name}-%{version}.tar.gz -C %{_tmppath}

%clean
rm -rf $RPM_BUILD_ROOT

%pre
if [ -f /etc/rsyslog.conf ] ; then
	mv /etc/rsyslog.conf /etc/rsyslog.conf.rpmsave
fi

%post
ln -s /etc/rsyslog-appliance.conf /etc/rsyslog.conf
/sbin/chkconfig rsyslog on
/sbin/service rsyslog start

%files
%defattr(-,root,root,-)

/etc/rsyslog-appliance.conf
/etc/rsyslog-collector.conf

%changelog
