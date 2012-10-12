Name:		medcommons-schema_db
Version:	0.0.340
Release:	1
Summary:	MedCommons Schema (DB only)

Group:		MedCommons
License:	MedCommons License

Source0:	%{name}-%{version}.tar.gz
BuildRoot:	%{_tmppath}/%{name}-%{version}
BuildArch:	noarch

Requires:	mysql mysql-server

%description
Schema for the MedCommons appliance.

%install
tar xzf $RPM_SOURCE_DIR/%{name}-%{version}.tar.gz -C %{_tmppath}

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf $RPM_BUILD_DIR/%{name}-%{version}

%pre
if [ "$1" -gt 1 ]; then
   pushd /root/schema_db
   ls -1 ???_*.sql > /tmp/medcommons-schema_db.root.sql.pre
   popd
fi

%post
if [ "$1" -gt 1 ]; then
   pushd /root/schema_db
   sh forge-update.sh | tee /var/local/schema-%{name}-%{version}.log
   popd
fi

%files
%defattr(-,root,root,-)
/root/schema_db

%changelog
