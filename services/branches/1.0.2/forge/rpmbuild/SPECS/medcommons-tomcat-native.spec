Name:		medcommons-tomcat-native
Version:	1.1.10
Release:	1
Summary:	tomcat-native for Apache Tomcat packaged for MedCommons

Group:		MedCommons
License:	Apache License

Source0:	%{name}-%{version}.tar.gz
BuildArch:	i386

Requires:	jdk openssl apr medcommons-tomcat
BuildRequires:	gcc openssl-devel apr-devel

%description
tomcat-native for Apache Tomcat as packaged for the MedCommons appliance.

%prep
%setup -q

%build
cd jni/native
JAVA_HOME=/usr/java/default ./configure --with-apr=/usr/local/bin/apr-1-config --prefix=$RPM_BUILD_ROOT/usr
make

%install
cd jni/native
#JAVA_HOME=/usr/java/default ./configure --with-apr=/usr/local/bin/apr-1-config --prefix=$RPM_BUILD_ROOT/usr
make install

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
/usr/lib/libtcnative-1.a
/usr/lib/libtcnative-1.la
/usr/lib/libtcnative-1.so
/usr/lib/libtcnative-1.so.0
/usr/lib/libtcnative-1.so.0.1.10
/usr/lib/pkgconfig/tcnative-1.pc

%changelog
* Wed Feb 04 2010 Nick Vasilatos <boxer@nickvasilatos.com> - tomcat-native-1.1.10
- Restored the build.
* Mon Oct 15 2007 Donald Way <donaldway@gmail.comn> - tomcat-native-1.1.10
- Initial build.
