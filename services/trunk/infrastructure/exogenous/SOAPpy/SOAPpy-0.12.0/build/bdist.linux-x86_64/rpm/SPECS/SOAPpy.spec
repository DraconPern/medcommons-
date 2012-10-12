%define name SOAPpy
%define version 0.12.0
%define release 1

Summary: SOAP Services for Python
Name: %{name}
Version: %{version}
Release: %{release}
Source0: %{name}-%{version}.tar.gz
License: UNKNOWN
Group: Development/Libraries
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Prefix: %{_prefix}
BuildArch: noarch
Vendor: Gregory Warnes <Gregory.R.Warnes@Pfizer.com>
Url: http://pywebsvcs.sf.net/

%description
SOAPpy provides tools for building SOAP clients and servers.  For more information see http://pywebsvcs.sf.net/

%prep
%setup

%build
python setup.py build

%install
python setup.py install --optimize 1 --root=$RPM_BUILD_ROOT --record=INSTALLED_FILES


%clean
rm -rf $RPM_BUILD_ROOT

%files -f INSTALLED_FILES
%defattr(-,root,root)
