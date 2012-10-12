Name:		medcommons-developers
Version:	0.2.17
Release:	1
Summary:	Configure accounts for MedCommons Operations Staff

Group:		MedCommons
License:	MedCommons License

Source0:	%{name}-%{version}.tar.gz
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:	noarch

Requires:	openssh pam shadow-utils sudo

Requires(pre):	/usr/sbin/useradd
Requires(pre):	/bin/sed
Requires(pre):	/bin/grep
Requires(pre):	/usr/bin/rsync
Requires(pre):	/etc/pam.d/login
Requires(pre):	/etc/security/access.conf
Requires(pre):	/etc/sudoers
Requires(pre):	/etc/ssh/sshd_config

%description
Enables secure access to deployed appliances by authorized operators.

%prep
%setup

%build

%install
install -DT -m 600 boxer $RPM_BUILD_ROOT/home/boxer/.ssh/authorized_keys &>/dev/null || :

%clean
rm -rf $RPM_BUILD_ROOT

%pre
if [ "$1" = 1 ]; then
    mkdir -p /etc/medcommons/restore
    cp /etc/ssh/sshd_config /etc/pam.d/login /etc/security/access.conf /etc/sudoers /etc/medcommons/restore

	#
	# Don't disable root login if we're going to pre-install packages (would prevent initial login to new appliance)
	#
    # sed -i -e 's/^#\(PermitRootLogin[ \t]*\)yes/\1no/' /etc/ssh/sshd_config || :
    # sed -i -e 's/^\(PasswordAuthentication[ \t]*\)yes/\1no/' /etc/ssh/sshd_config || :
	#
    sed -i -e 's/^#\([ \t]*%wheel[ \t]*ALL=(ALL)[ \t]*ALL\)/\1/' /etc/sudoers || :
    if ! grep "^[ \t]*-[ \t]*:[ \t]*ALL[ \t]*EXCEPT[ \t]*root[ \t]*:[ \t]*LOCAL" /etc/security/access.conf &>/dev/null
    then
       echo "" >> /etc/security/access.conf || :
       echo "-:ALL EXCEPT root:LOCAL" >> /etc/security/access.conf || :
       echo "+:wheel:ALL" >> /etc/security/access.conf || :
       echo "-:ALL:-" >> /etc/security/access.conf || :
    fi
    sed -i -e 's/\(account[ \t]*required[ \t]*\)pam_nologin.so/\1pam_access.so/' /etc/pam.d/login || :

    mkdir /etc/skel/Maildir
    mkdir /etc/skel/Maildir/cur
    mkdir /etc/skel/Maildir/tmp
    mkdir /etc/skel/Maildir/new
    mkdir /etc/skel/.ssh

    chmod -R 0700 /etc/skel/Maildir
    chmod -R 0700 /etc/skel/.ssh
fi

/usr/sbin/useradd -c "Authorized Operator" -m -G wheel -p "Ju0k4X7nnc6OI" operator &>/dev/null || :
/usr/sbin/useradd -c "Nick Vasilatos" -m -G wheel -p "" boxer &>/dev/null || :

%files
%defattr(-,root,root,-)
%attr(0600, operator, operator) /home/operator/.ssh/authorized_keys
%attr(0600, boxer, boxer) /home/boxer/.ssh/authorized_keys

%changelog
* Fri Mar 27 2009 NVA <boxer@medcommons.net> 0.1
- Initial build.
