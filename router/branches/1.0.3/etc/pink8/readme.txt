Release Notes for /Pink6 Release on 25 Sept 04

There is not much new in the pinkbox, except for a couple of support tables

The purplebox simulator has some new features - it reports back every 10 seconds now 

The big think here is the Whitebox

The Whitebox is a re-implementation of the SCAR demo, but with a real database behind it and using distributed purple boxes for WADO viewing.

The registration system is working - you will need to register and deal with passwords, etc.

The only thing that is still really fake is the HIPAA log

The 3 studies are now in the XDS Registry and are available via the Selection Screen. TYSON is not found on the Purple Box, but the voxar studies work fine.

The only purple box involved here is at Adrian's place:mcpurple01

The Order Form has been engineered to fit in 744*744 and will be popped into place by the WADO viewer

The Selection screen scans the XDS Registry for its choices.

The O link will pop up an order form without the WADO viewer

The time/date link will invoke the WADO viewer, passing the GUID and lots of extra arguments which will be passed by WADO into the Order Form

If you can manage to give yourself the ADMIN role (you will need to mess with phpMyAdmin on virtual01) for your account then the system will display additional privileged links on the home page. If you want to change the links go edit the special_links table in /pink6.





