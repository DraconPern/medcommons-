Name:		medcommons-jai
Version:	1.1.38
Release:	1
Summary:	MedCommons JAI

Group:		MedCommons
License:	Sun Microsystems Binary Code License

Source0:	%{name}.tar.gz
BuildRoot:	%{_tmppath}/%{name}
BuildArch:	x86_64

Requires:	jdk

%description
JAI repack 

%prep
rm -rf $RPM_BUILD_ROOT

%install
tar xzf $RPM_SOURCE_DIR/%{name}.tar.gz -C %{_tmppath}

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
/usr/java/default/jre/lib/jai_codec.jar
/usr/java/default/jre/lib/jai_core.jar
/usr/java/default/jre/lib/libmlib_jai.so
/usr/java/default/jre/lib/mlibwrapper_jai.jar


%changelog
