Name:		medcommons-jsvc
Version:	6.0.48
Release:	1
Summary:	jsvc for Apache Tomcat packaged for MedCommons

Group:		MedCommons
License:	Apache License

Source0:	%{name}.tar.gz
BuildRoot:	%{_tmppath}/%{name}
BuildArch:	x86_64

Requires:	jdk medcommons-tomcat

%description
jsvc re-pack for the MedCommons appliance.

%prep
rm -rf $RPM_BUILD_ROOT

%install
mkdir %{_tmppath}/%{name}-%{version}
tar xzf $RPM_SOURCE_DIR/%{name}.tar.gz -C %{_tmppath}

%clean
rm -rf $RPM_BUILD_ROOT

%files
/opt/apache-tomcat/bin/jsvc
%attr(0755, root, root) /opt/apache-tomcat/bin/jsvc

%changelog
* Mon Oct 11 2010 nVa <boxer@medcommons.net> - jsvc-6.0.18
- No relation to the predecessor - first that more or less works.
