#!/usr/bin/perl

##############################################################################
#                                                                            #
# BFormMail                        Version 2.0                               #
#                                                                            #
# Copyright 1997-2001 Brian Sietz  bsietz@infosheet.com                      #
# The Byte Shop - Small Business Solutions for Internet Web Development      #
# http://www.infosheet.com                                                   #
# Created:  8/14/1997                                                        #
# Modified: 12/17/2001                                                       #
#                                                                            #
# Modifications Copyright (c) 1997-2001 Brian S. Sietz, All Rights Reserved. #
# This version of FormMail may be used and modified free of charge by anyone #
# so long as this copyright notice and the one below by Matthew Wright remain#
# intact. By using this code you agree to indemnify Brian Sietz from any     #
# liability arising from it's use. You also agree that this code cannot be   #
# sold to any third party without prior written consent of both Brian Sietz  #
# and Matthew M. Wright.						     #
#                                                                            #
##############################################################################
# FormMail                        Version 1.9                                #
# Copyright 1995-2001 Matt Wright mattw@worldwidemart.com                    #
# Created 06/09/95                Last Modified 08/03/01                     #
# Matt's Script Archive, Inc.:    http://www.worldwidemart.com/scripts/      #
##############################################################################
# COPYRIGHT NOTICE                                                           #
# Copyright 1995-2001 Matthew M. Wright  All Rights Reserved.                #
#                                                                            #
# FormMail may be used and modified free of charge by anyone so long as this #
# copyright notice and the comments above remain intact.  By using this      #
# code you agree to indemnify Matthew M. Wright from any liability that      #
# might arise from its use.                                                  #
#                                                                            #
# Selling the code for this program without prior written consent is         #
# expressly forbidden.  In other words, please ask first before you try and  #
# make money off of my program.                                              #
#                                                                            #
# Obtain permission before redistributing this software over the Internet or #
# in any other medium.	In all cases copyright and header must remain intact #
##############################################################################
# ACCESS CONTROL FIX: Peter D. Thompson Yezek                                #
#                     http://www.securityfocus.com/archive/1/62033           #
##############################################################################
#                                                                            #
#                                                                            #
# BFormMail                                                                  #
#                                                                            #
#      Took Matt's original 1.6 script and made some mods...                 #
#      Then took Matt's 1.9 and added the security features here             #
#                                                                            #
#      Mods made were mostly from features in yForm                          #
#      which was Matt's original FormMail 1.5 with changes by:               #
#      Donald E. Killen 10/2/96 and                                          #
#      Ashley Bass 1/29/97                                                   #
#                                                                            #
# History:                                                                   #
#                                                                            #
#   Added 6/29/97:                                                           #
#      - Added table output to HTML (orig by Don Killen in yForm)            #
#      - Added printing of realname & email in HTML output (orig Ashley Bass)#
#      - Added misc form fields:                                             #
#          cc  - if present, a Cc: is added to the e-mail when sent          #
#          bcc - if present, a Bcc: is added to the e-mail when sent         #
#      - Added courtesy reply (based on code from yForm)                     #
#        Changed field names; a bit longer, but easier to understand:        #
#          courtesy_reply - if present and email also present, reply sent    #
#          courtesy_reply_texta, First line of courtesy reply text           #
#          courtesy_reply_textb, Second line of courtesy reply text          #
#          courtesy_who_we_are, Name or company underneath the "Regards"     #
#          courtesy_our_url, URL to print after "Regards"                    #
#          courtesy_our_email, e-mail to print after "Regards"               #
#      - Added database option (based on code from yForm)                    #
#          append_db, if present, value is the data file to append to        #
#          db_delimiter, delimiter between fields                            #
#      - Removed FormMail display in HTML output (except error output)       #
#          Nobody should care about who wrote the script, if they really     #
#          want to know, they should send e-mail to the webmaster...         #
#                                                                            #
#   Added 8/14/97:                                                           #
#      - Added support for e-mail to fax services by adding two form fields: #
#          faxto, if specified is the e-mail address of the fax service.     #
#                 for Faxaway, it would be a phone number@faxaway.com, i.e.  #
#                 16097951994@faxaway.com                                    #
#          faxfrom, specifies the From: field for the fax.  Faxaway requires #
#                 field to be a valid Faxaway customer.                      #
#        More information can be found in the BFormMail.readme file or       #
#        at http://www/faxaway.com                                           #
#      - Added db_fields config field to control which fields are appended   #
#        to the database.                                                    #
#      - All form fields appended to database are stripped of newlines so    #
#        that all outputted fields will be on a single record                #
#                                                                            #
#   Added 1/27/98:                                                           #
#      - Added courtesy_who_we_are2 - same as courtesy_who_we_are but an     #
#        extra line of text if needed.                                       #
#      - Added support for another e-mail to fax service.  Fax service is    #
#        selected by the faxservice field.  Currently, the faxservice field  #
#        can specify 'faxaway' or 'faxsav' or 'netmoves'. Each service       #
#        requires a slightly (faxsav & netmoves are the same)                #
#        different header. The following fields fully control the form-fax   #
#        gateway:                                                            #
#          faxservice, if specified enables the form-to-fax gateway and will #
#             specify the desired service.  The current services supported   #
#             are 'faxsav', 'netmoves' and 'faxaway'.                        # 
#             For more information on these                                  #
#             services visit http://www.netmoves.com or                      #
#             http://www.faxaway.com                                         #
#             Please note, faxsav  requires the variable $faxstamp           #
#             to be set - see below.                                         #
#          faxnum, specifies the telephone number to send the fax.  For      #
#             security, the full e-mail address is assembled in the script.  #
#             Both faxsav & faxaway require the format as follows:           #
#             16095551212                                                    #
#          faxfrom, specifies the From: field for the fax.  Must be from an  #
#             authorized account from both services.   For example:          #
#             bsietz@infosheet.com                                           #
#        More information can be found in the BFormMail.readme file.         #
#                                                                            #
#   Added 7/16/98:                                                           #
#      - Added check for valid e-mail address, if specified for cc: & bcc:   #
#                                                                            #
#   Added 12/9/98:                                                           #
#      - Fixed bug in print_blank_fields                                     #
#                                                                            #
#   Added 8/15/99:                                                           #
#      - Y2K fix provided by Karl Bogott                                     #
#                                                                            #
#   Added 10/10/99:                                                          #
#      - In routine check_url, if HTTP_REFERER not available, no longer      #
#        return true.                                                        #
#                                                                            #
#   Added 3/12/2000:                                                         #
#      - Fixed ?? bug in redirect tag                                        #
#                                                                            #
#   Added 4/18/2000:                                                         #
#      - Added cc_visitor tag - send copy of form results to visitor         #
#                                                                            #
#   Added 1/22/2001:                                                         #
#      - Modified fax to support netmoves (formerly faxsav)                  #
#                                                                            #
#   Added 9/16/2001:                                                         #
#      - Added EasyLink to list of fax services (formerly netmoves or faxsav #
#                                                                            #
#   Added 12/2001:                                                           #
#      - Added security fixes from FormMail Version 1.9                      #
#                                                                            #
##############################################################################

# Define Variables                                                           #
#	 Detailed Information Found In README File.                          #

# $mailprog defines the location of your sendmail program on your unix       #
# system.                                                                    #

$mailprog = '/usr/sbin/sendmail -oi -t';

# @referers allows forms to be located only on servers which are defined     #
# in this field.  This security fix from the last version which allowed      #
# anyone on any server to use your FormMail script on their web site.        #

@referers = ('\w');

# @recipients defines the e-mail addresses or domain names that e-mail can   #
# be sent to.  This must be filled in correctly to prevent SPAM and allow    #
# valid addresses to receive e-mail.  Read the documentation to find out how #
# this variable works!!!  It is EXTREMELY IMPORTANT.                         #
@recipients = ('\w');


# ACCESS CONTROL FIX: Peter D. Thompson Yezek                                #
# @valid_ENV allows the sysadmin to define what environment variables can    #
# be reported via the env_report directive.  This was implemented to fix     #
# the problem reported at http://www.securityfocus.com/bid/1187              #

@valid_ENV = ('REMOTE_HOST','REMOTE_ADDR','REMOTE_USER','HTTP_USER_AGENT');

# BSS
# The EasyLink service requires a special stamp as part of the e-mail header #
# for additional security.  This stamp, along with the appropriate 'from'    #
# field are required in order to send a fax.                                 #
#                                                                            #
# Replace passwd in the line below with the stamp issued from EasyLink.      #
# visit http://www.EasyLink.com for more information.                        #

$faxstamp = 'passwd';

# Done                                                                       #
##############################################################################


# Check Referring URL
&check_url;


# Retrieve Date
&get_date;

# Parse Form Contents
&parse_form;

# Check Required Fields
&check_required;

# Send E-Mail
&send_mail;

#BSS
# Courtesy E-Mail to Visitor
&send_courtesy;

#Append Database
&append_database;

# Send E-Fax
if ($Config{'faxservice'}) {
    &send_mail($Config{'faxservice'})
};

# Return HTML Page or Redirect User
&return_html;

#BSS

# Main ends here - only subroutines follow                                   #
##############################################################################

sub check_url {

    # Localize the check_referer flag which determines if user is valid.     #
    local($check_referer) = 0;

    # If a referring URL was specified, for each valid referer, make sure    #
    # that a valid referring URL was passed to FormMail.                     #

    if ($ENV{'HTTP_REFERER'}) {
        foreach $referer (@referers) {
            if ($ENV{'HTTP_REFERER'} =~ m|https?://([^/]*)$referer|i) {
                $check_referer = 1;
                last;
            }
        }
    }
    else {
        $check_referer = 1;
    }

    # If the HTTP_REFERER was invalid, send back an error.                   #
    if ($check_referer != 1) { &error('bad_referer') }

}

sub get_date {

    # Define arrays for the day of the week and month of the year.           #
    @days   = ('Sunday','Monday','Tuesday','Wednesday',
               'Thursday','Friday','Saturday');
    @months = ('January','February','March','April','May','June','July',
	         'August','September','October','November','December');

    # Get the current time and format the hour, minutes and seconds.  Add    #
    # 1900 to the year to get the full 4 digit year.                         #
    ($sec,$min,$hour,$mday,$mon,$year,$wday) = (localtime(time))[0,1,2,3,4,5,6];
    $time = sprintf("%02d:%02d:%02d",$hour,$min,$sec);

    # $year += 1900;
    # Y2K fix provided by Karl Bogott 8/1999
    if ($year < 50){
	$year += 2000;
    }
    else {
	$year += 1900;
    }

    # Format the date.                                                       #
    $date = "$days[$wday], $months[$mon] $mday, $year at $time";
    $mon2 = $mon + 1;
    $date2 = "$mon2/$mday/$year";
}

sub parse_form {

    # Define the configuration associative array.                            #
    %Config = ('recipient','',          'subject','',
               'email','',              'realname','',
               'redirect','',           'bgcolor','',
               'background','',         'link_color','',
               'vlink_color','',        'text_color','',
               'alink_color','',        'title','',
               'sort','',               'print_config','',
               'required','',           'env_report','',
               'return_link_title','',  'return_link_url','',
               'print_blank_fields','', 'missing_fields_redirect','',
#BSS
               'cc','',	                'bcc','',
	       'courtesy_reply','',
	       'courtesy_our_url','',   'courtesy_our_email','',
	       'courtesy_reply_texta','',
	       'courtesy_reply_textb','',
	       'courtesy_who_we_are','','courtesy_who_we_are2','',
	       'append_db','',          'db_delimiter','',
	       'db_fields','',
	       'faxservice','',
	       'faxnum','',              'faxfrom','',
	       'cc_visitor',''
#BSS
	   );

    # Determine the form's REQUEST_METHOD (GET or POST) and split the form   #
    # fields up into their name-value pairs.  If the REQUEST_METHOD was      #
    # not GET or POST, send an error.                                        #
    if ($ENV{'REQUEST_METHOD'} eq 'GET') {
        # Split the name-value pairs
        @pairs = split(/&/, $ENV{'QUERY_STRING'});
    }
    elsif ($ENV{'REQUEST_METHOD'} eq 'POST') {
        # Get the input
        read(STDIN, $buffer, $ENV{'CONTENT_LENGTH'});
 
        # Split the name-value pairs
        @pairs = split(/&/, $buffer);
    }
    else {
        &error('request_method');
    }

    # For each name-value pair:                                              #
    foreach $pair (@pairs) {

        # Split the pair up into individual variables.                       #
        local($name, $value) = split(/=/, $pair);
 
        # Decode the form encoding on the name and value variables.          #
        $name =~ tr/+/ /;
        $name =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;

        $value =~ tr/+/ /;
        $value =~ s/%([a-fA-F0-9][a-fA-F0-9])/pack("C", hex($1))/eg;

        # If they try to include server side includes, erase them, so they
        # aren't a security risk if the html gets returned.  Another 
        # security hole plugged up.
        $value =~ s/<!--(.|\n)*-->//g;

        # If the field name has been specified in the %Config array, it will #
        # return a 1 for defined($Config{$name}}) and we should associate    #
        # this value with the appropriate configuration variable.  If this   #
        # is not a configuration form field, put it into the associative     #
        # array %Form, appending the value with a ', ' if there is already a #
        # value present.  We also save the order of the form fields in the   #
        # @Field_Order array so we can use this order for the generic sort.  #
        if (defined($Config{$name})) {
            $Config{$name} = $value;
        }
        else {
            if ($Form{$name} && $value) {
                $Form{$name} = "$Form{$name}, $value";
            }
#BSS - Bug fix provided by JJ Steward: below line prevents print_blank_fields
#from working correctly.
#            elsif ($value) {
            else {
                push(@Field_Order,$name);
                $Form{$name} = $value;
            }
        }
    }

    # The next six lines remove any extra spaces or new lines from the       #
    # configuration variables, which may have been caused if your editor     #
    # wraps lines after a certain length or if you used spaces between field #
    # names or environment variables.                                        #
    $Config{'required'} =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
    $Config{'required'} =~ s/(\s+)?\n+(\s+)?//g;
    $Config{'env_report'} =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
    $Config{'env_report'} =~ s/(\s+)?\n+(\s+)?//g;
    $Config{'print_config'} =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
    $Config{'print_config'} =~ s/(\s+)?\n+(\s+)?//g;
    $Config{'db_fields'} =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
    $Config{'db_fields'} =~ s/(\s+)?\n+(\s+)?//g;

    # Split the configuration variables into individual field names.         #
    @Required = split(/,/,$Config{'required'});
    @Env_Report = split(/,/,$Config{'env_report'});
    @Print_Config = split(/,/,$Config{'print_config'});
    @Print_DB = split(/,/,"$Config{'db_fields'},$Form{'db_fields'}");

    # ACCESS CONTROL FIX: Only allow ENV variables in @valid_ENV in          #
    # @Env_Report for security reasons.                                      #
    foreach $env_item (@Env_Report) {
        foreach $valid_item (@valid_ENV) {
            if ( $env_item eq $valid_item ) { push(@temp_array, $env_item) }
        }
    } 
    @Env_Report = @temp_array;

}

sub check_required {

    # Localize the variables used in this subroutine.                        #
    local($require, $recipient2, @error );

    $recipient2 = $Config{'recipient'};

# FormMail & BFormMail allows for a recipient email address in the form:
# "bsietz@infosheet.com (Brian Sietz)" so that any email received by sendmail
# would have in the header:    To: Brian Sietz (bsietz@infosheet.com).  
# Until Matt Wright added his security enhancements in FormMail v1.9, this
# worked ok, however the recipient check below would fail with the added
# name in parenthesis.  The following two lines below remove any spaces at
# the beginning of the string, and delete all characters following a space
# if it exists.  The recipient is then check against the valid recipients.
# The original recipient string remains unchanged for use in the email routines

    $recipient2 =~ s/^\s+|\s+$//g;
    $recipient2 =~ s/\s.*//;

    if ($Config{'subject'} =~ /(\n|\r)/m || 
        $recipient2 =~ /(\n|\r)/m) {
        &error('no_recipient');
    }

    if (!$recipient2) {
        if (!defined(%Form)) { &error('bad_referer') }
        else                 { &error('no_recipient') }
    }
    else {
        # This block of code requires that the recipient address end with    #
        # a valid domain or e-mail address as defined in @recipients.        #
        $valid_recipient = 0;
        foreach $send_to (split(/,/,$recipient2)) {
            foreach $recipient (@recipients) {
                if ($send_to =~ /$recipient$/i) {
                    push(@send_to,$send_to); last;
                }
            }
        }
        if ($#send_to < 0) { &error('no_recipient') }
        $recipient2 = join(',',@send_to);
    }

    # For each require field defined in the form:                            #
    foreach $require (@Required) {

        # If the required field is the email field, the syntax of the email  #
        # address if checked to make sure it passes a valid syntax.          #
        if ($require eq 'email' && !&check_email($Config{$require})) {
            push(@error,$require);
        }

        # Otherwise, if the required field is a configuration field and it   #
        # has no value or has been filled in with a space, send an error.    #
        elsif (defined($Config{$require})) {
            if (!$Config{$require}) {
                push(@error,$require);
            }
        }

        # If it is a regular form field which has not been filled in or      #
        # filled in with a space, flag it as an error field.                 #
        elsif (!$Form{$require}) {
            push(@error,$require);
        }
    }

    # If any error fields have been found, send error message to the user.   #
    if (@error) { &error('missing_fields', @error) }
}

sub return_html {
    # Local variables used in this subroutine initialized.                   #
    local($key,$sort_order,$sorted_field);

    # If redirect option is used, print the redirectional location header.   #
    if ($Config{'redirect'}) {
        print "Location: $Config{'redirect'}\n\n";
    }

    # Otherwise, begin printing the response page.                           #
    else {

        # Print HTTP header and opening HTML tags.                           #
        print "Content-type: text/html\n\n";
        print "<html>\n <head>\n";

        # Print out title of page                                            #
        if ($Config{'title'}) { print "  <title>$Config{'title'}</title>\n" }
        else                  { print "  <title>Thank You</title>\n"        }

        print " </head>\n <body";

        # Get Body Tag Attributes                                            #
        &body_attributes;

        # Close Body Tag                                                     #
        print ">\n  <center>\n";

        # Print custom or generic title.                                     #
        if ($Config{'title'}) { print "   <h1>$Config{'title'}</h1>\n" }
        else { print "   <h1>Thank You For Your Interest in the Widget Download</h1>\n" }

        print "</center>\n";

        print "The following message was submitted $Config{'recipient'} ";
        print "<br>on $date<p><hr size=1 width=75\%><p>\n";

        #BSS Table output for HTML (orig Don Killen) 
        #    Also realname and email fields (orig Ashley Bass)
        print "<table cellspacing=2 cellpadding=1>";
	if ($Config{'realname'}) {
            print "<tr><td align=right><b>Name:</b></td>";
	    print "<td align=left>$Config{'realname'}</td></tr>\n"
        }
        
        if ($Config{'email'}) {
            print "<tr><td align=right><b>E-mail:</b></td>";
	    print "<td align=left>$Config{'email'}</td></tr>\n\n"
        }
        #BSS

        # Sort alphabetically if specified:                                  #
        if ($Config{'sort'} eq 'alphabetic') {
            foreach $field (sort keys %Form) {

                # If the field has a value or the print blank fields option  #
                # is turned on, print out the form field and value.          #
                if ($Config{'print_blank_fields'} || $Form{$field}) {
                    #BSS - table output
                    #print "<b>$field:</b> $Form{$field}<p>\n";
                    print "<tr><td align=right>$field:</td>";
		    print "<td align=left>$Form{$field}</td></tr>\n";
		    #BSS
                }
            }
        }

        # If a sort order is specified, sort the form fields based on that.  #
        elsif ($Config{'sort'} =~ /^order:.*,.*/) {

            # Set the temporary $sort_order variable to the sorting order,   #
            # remove extraneous line breaks and spaces, remove the order:    #
            # directive and split the sort fields into an array.             #
            $sort_order = $Config{'sort'};
            $sort_order =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
            $sort_order =~ s/(\s+)?\n+(\s+)?//g;
            $sort_order =~ s/order://;
            @sorted_fields = split(/,/, $sort_order);

            # For each sorted field, if it has a value or the print blank    #
            # fields option is turned on print the form field and value.     #
            foreach $sorted_field (@sorted_fields) {
                if ($Config{'print_blank_fields'} || $Form{$sorted_field}) {
                    #BSS - table output
                    #print "<b>$sorted_field:</b> $Form{$sorted_field}<p>\n";
                    print "<tr><td align=right>$sorted_field:</td>";
		    print "<td align=left>$Form{$sorted_field}</td></tr>\n";
		    #BSS
                }
            }
        }

        # Otherwise, default to the order in which the fields were sent.     #
        else {

            # For each form field, if it has a value or the print blank      #
            # fields option is turned on print the form field and value.     #
            foreach $field (@Field_Order) {
                if ($Config{'print_blank_fields'} || $Form{$field}) {
                    #BSS - table output
                    #print "<b>$field:</b> $Form{$field}<p>\n";
                    print "<tr><td align=right><b>$field:</b></td>";
		    print "<td align=left>$Form{$field}</td></tr>\n";
		    #BSS
                }
            }
        }

#BSS
        print "</table><br clear=all>\n";
#BSS


        print "<p><hr size=1 width=75%><p>\n";

        # Check for a Return Link and print one if found.                    #
        if ($Config{'return_link_url'} && $Config{'return_link_title'}) {
            print "<ul>\n";
            print "<li><a href=\"$Config{'return_link_url'}\">$Config{'return_link_title'}</a>\n";
            print "</ul>\n";
        }

        # Print the page footer.                                             #
        print <<"(END HTML FOOTER)";
        <hr size=1 width=75%><p> 
        </body>
       </html>
(END HTML FOOTER)
    }
}

sub send_mail {
    # Localize variables used in this subroutine.                            #

#BSS
    local ($faxservice) = @_;

    local($print_config,$key,$sort_order,$sorted_field,$env_report);

    # Open The Mail Program
    open(MAIL,"|$mailprog -t");

    if ($faxservice)  {
	if ($faxservice eq 'faxaway') {
        print MAIL "To: $Config{'faxnum'}\@faxaway.com\n";
	print MAIL "From: $Config{'faxfrom'}\n";
        }

	if ( ($faxservice eq 'faxsav') || 
	     ($faxservice eq 'netmoves') || 
	     ($faxservice eq 'easylink') ) {
	print MAIL "To: $Config{'faxnum'}\@faxmail.com\n";
	print MAIL "X-STAMP: $faxstamp\n";
	print MAIL "X-FAXSENDER: $Config{'faxfrom'}\n";
        }
    }
    else {
        print MAIL "To: $Config{'recipient'}\n";
        print MAIL "From: $Config{'email'} ($Config{'realname'})\n";
	if ($Config{'cc'} && check_email($Config{'cc'}))
        	{ print MAIL "Cc: $Config{'cc'}\n" };
	if ($Config{'cc_visitor'})
	        { print MAIL "Cc: $Config{'email'} ($Config{'realname'})\n"};
        if ($Config{'bcc'} && check_email($Config{'bcc'}))
		{ print MAIL "Bcc: $Config{'bcc'}\n" };
    }
#BSS

    # Check for Message Subject
    if ($Config{'subject'}) { print MAIL "Subject: $Config{'subject'}\n\n" }
    else                    { print MAIL "Subject: WWW Form Submission\n\n" }

    print MAIL "Below is the result of your feedback form:\n";
    print MAIL "    It was submitted by: $Config{'realname'} ($Config{'email'})\n    on $date\n";

#BSS
    if ($Config{'faxservice'}) {
    print MAIL "Feedback results were also faxed to: $Config{'faxnum'}\n";
    }
#BSS

    print MAIL "-" x 75 . "\n\n";

    if (@Print_Config) {
        foreach $print_config (@Print_Config) {
            if ($Config{$print_config}) {
                print MAIL "$print_config: $Config{$print_config}\n\n";
            }
        }
    }

    # Sort alphabetically if specified:                                      #
    if ($Config{'sort'} eq 'alphabetic') {
        foreach $field (sort keys %Form) {

            # If the field has a value or the print blank fields option      #
            # is turned on, print out the form field and value.              #
            if ($Config{'print_blank_fields'} || $Form{$field} ||
                $Form{$field} eq '0') {
                print MAIL "$field: $Form{$field}\n\n";
            }
        }
    }

    # If a sort order is specified, sort the form fields based on that.      #
    elsif ($Config{'sort'} =~ /^order:.*,.*/) {

        # Remove extraneous line breaks and spaces, remove the order:        #
        # directive and split the sort fields into an array.                 #
        $Config{'sort'} =~ s/(\s+|\n)?,(\s+|\n)?/,/g;
        $Config{'sort'} =~ s/(\s+)?\n+(\s+)?//g;
        $Config{'sort'} =~ s/order://;
        @sorted_fields = split(/,/, $Config{'sort'});

        # For each sorted field, if it has a value or the print blank        #
        # fields option is turned on print the form field and value.         #
        foreach $sorted_field (@sorted_fields) {
            if ($Config{'print_blank_fields'} || $Form{$sorted_field} ||
                $Form{$sorted_field} eq '0') {
                print MAIL "$sorted_field: $Form{$sorted_field}\n\n";
            }
        }
    }

    # Otherwise, default to the order in which the fields were sent.         #
    else {

        # For each form field, if it has a value or the print blank          #
        # fields option is turned on print the form field and value.         #
        foreach $field (@Field_Order) {
            if ($Config{'print_blank_fields'} || $Form{$field} ||
                $Form{$field} eq '0') {
                print MAIL "$field: $Form{$field}\n\n";
            }
        }
    }

    print MAIL "-" x 75 . "\n\n";

    # Send any specified Environment Variables to recipient.                 #
    foreach $env_report (@Env_Report) {
        if ($ENV{$env_report}) {
            print MAIL "$env_report: $ENV{$env_report}\n";
        }
    }

    close (MAIL);
}

sub check_email {
    # Initialize local email variable with input to subroutine.              #
    $email = $_[0];

    # If the e-mail address contains:                                        #
    if ($email =~ /(@.*@)|(\.\.)|(@\.)|(\.@)|(^\.)/ ||

        # the e-mail address contains an invalid syntax.  Or, if the         #
        # syntax does not match the following regular expression pattern     #
        # it fails basic syntax verification.                                #

        $email !~ /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z0-9]+)(\]?)$/) {

        # Basic syntax requires:  one or more characters before the @ sign,  #
        # followed by an optional '[', then any number of letters, numbers,  #
        # dashes or periods (valid domain/IP characters) ending in a period  #
        # and then 2 or 3 letters (for domain suffixes) or 1 to 3 numbers    #
        # (for IP addresses).  An ending bracket is also allowed as it is    #
        # valid syntax to have an email address like: user@[255.255.255.0]   #

        # Return a false value, since the e-mail address did not pass valid  #
        # syntax.                                                            #
        return 0;
    }

    else {

        # Return a true value, e-mail verification passed.                   #
        return 1;
    }
}

sub body_attributes {
    # Check for Background Color
    if ($Config{'bgcolor'}) { print " bgcolor=\"$Config{'bgcolor'}\"" }

    # Check for Background Image
    if ($Config{'background'}) { print " background=\"$Config{'background'}\"" }

    # Check for Link Color
    if ($Config{'link_color'}) { print " link=\"$Config{'link_color'}\"" }

    # Check for Visited Link Color
    if ($Config{'vlink_color'}) { print " vlink=\"$Config{'vlink_color'}\"" }

    # Check for Active Link Color
    if ($Config{'alink_color'}) { print " alink=\"$Config{'alink_color'}\"" }

    # Check for Body Text Color
    if ($Config{'text_color'}) { print " text=\"$Config{'text_color'}\"" }
}

#############################################################################
#                                                                           #
# BSS: Send courtesy email to the visitor thanking him, etc.                #
#                                                                           #
#      Not sure if this portion of code was written by Ashley Bass or by    #
#      Donald Killen, but was taken from yForm                              #
#                                                                           #
#      Code is basically the same, just some variable name changes to be    #
#      more self explainatory.                                              #
#                                                                           #

sub send_courtesy {
  if ($Config{'courtesy_reply'} && $Config{'email'})
 { 
   open (MAIL,"|$mailprog -t");
   print MAIL "To: $Config{'email'} ($Config{'realname'})\n";
   print MAIL "From: $Config{'courtesy_our_email'}\n";

   if ($Config{'subject'}) {
      print MAIL "Subject: User and Password Info\n\n";
      $subjflag = 1;
   }
   else {
      print MAIL "Subject: Thank you - $date\n\n";
      $subjflag = 0;
   }
   print MAIL "Thank you for your interest in the medcommons viewer demo.\n Please click on the link below and enter the username and password provided.\n";
   if ($Config{'courtesy_reply_texta'}) {
      print MAIL "$Config{'courtesy_reply_texta'}\n";
   }
   if ($Config{'courtesy_reply_textb'}) {
      print MAIL "$Config{'courtesy_reply_textb'}\n\n";
   }
   print MAIL "Regards,\n";
   print MAIL "$Config{'courtesy_who_we_are'}\n";
   print MAIL "$Config{'courtesy_who_we_are2'}\n";
   print MAIL "$Config{'courtesy_our_email'}\n";
   print MAIL "$Config{'courtesy_our_url'}\n";
   close (MAIL);
}
}


#############################################################################
#                                                                           #
# BSS: Append to a Database file                                            #
#                                                                           #
#      Originally appeared in yForm written by Ashley Bass 1/29/97          #
#                                                                           #

sub append_database {

    local($print_db,$field);

 if ($Config{'append_db'})
  {
    if (-w $Config{'append_db'})
    {

        &lockit ("$Config{'append_db'}.lock");

	open (DATABASE, ">>$Config{'append_db'}");
	print DATABASE "$Config{'db_delimiter'}";
	print DATABASE "$date2$Config{'db_delimiter'}";
        print DATABASE "$time$Config{'db_delimiter'}";

        foreach $print_db (@Print_DB) {
            if ($Config{$print_db}) {
	        $field = $Config{$print_db};
		$field =~ s/\r\n/ /gs;
	        print DATABASE "$field";
	    }
	    if ($Form{$print_db}) {
	        $field = $Form{$print_db};
		$field =~ s/\r\n/ /gs;
	        print DATABASE "$field";
	    };

	print DATABASE "$Config{'db_delimiter'}";

        };

        print DATABASE "\n"; 
    close (DATABASE);

    &unlockit ("$Config{'append_db'}.lock");

   }
 }
}

sub lockit
  {
  local ($lock_file) = @_;
  local ($endtime);
  $endtime = 20;
  $endtime = time + $endtime;

  while (-e $lock_file && time < $endtime)
    {
    sleep(1);
    }           

  open(LOCK_FILE, ">$lock_file") || &file_open_error ("$lock_file", 
						      "Lock File Routine",
						      __FILE__, __LINE__);

# flock(LOCK_FILE, 2); # 2 exclusively locks the file
  }

#######################################################################
sub unlockit
  {
  local ($lock_file) = @_;

# flock(LOCK_FILE, 8); # 8 unlocks the file

  close(LOCK_FILE);
  unlink($lock_file);
  } 

#######################################################################
sub file_open_error
  {
  local ($bad_file, $script_section, $this_file, $line_number) = @_;
  print "Content-type: text/html\n\n";
  &CgiDie ("I am sorry, but I was not able to access $bad_file.")
  }     



sub error { 
    # Localize variables and assign subroutine input.                        #
    local($error,@error_fields) = @_;
    local($host,$missing_field,$missing_field_list);

    if ($error eq 'bad_referer') {
        if ($ENV{'HTTP_REFERER'} =~ m|^https?://([\w\.]+)|i) {
            $host = $1;
            print <<"(END ERROR HTML)";
Content-type: text/html

<html>
 <head>
  <title>Bad Referrer - Access Denied</title>
 </head>
 <body bgcolor=#FFFFFF text=#000000>
  <center>
   <table border=0 width=600 bgcolor=#9C9C9C>
    <tr><th><font size=+2>Bad Referrer - Access Denied</font></th></tr>
   </table>
   <table border=0 width=600 bgcolor=#CFCFCF>
    <tr><td>The form attempting to use
     <a href="http://www.worldwidemart.com/scripts/formmail.shtml">FormMail</a>
     resides at <tt>$ENV{'HTTP_REFERER'}</tt>, which is not allowed to access
     this cgi script.<p>

     If you are attempting to configure FormMail to run with this form, you need
     to add the following to \@referers, explained in detail in the README file.<p>

     Add <tt>'$host'</tt> to your <tt><b>\@referers</b></tt> array.<hr size=1>
     <center><font size=-1>
      <a href="http://www.worldwidemart.com/scripts/formmail.shtml">FormMail</a> V1.9 &copy; 1995 - 2001  Matt Wright<br>
      A Free Product of <a href="http://www.worldwidemart.com/scripts/">Matt's Script Archive, Inc.</a>
     </font></center>
    </td></tr>
   </table>
  </center>
 </body>
</html>
(END ERROR HTML)
        }
        else {
            print <<"(END ERROR HTML)";
Content-type: text/html

<html>
 <head>
  <title>BFormMail v2.0</title>
 </head>
 <body bgcolor=#FFFFFF text=#000000>
  <center>
   <table border=0 width=600 bgcolor=#9C9C9C>
    <tr><th><font size=+2>FormMail -- BFormMail</font></th></tr>
   </table>
   <table border=0 width=600 bgcolor=#CFCFCF>
    <tr><th><tt><font size=+1>Copyright 1995 - 2001 Matt Wright<br>
        Version 1.9 - Released August 03, 2001<br>
        A Free Product of <a href="http://www.worldwidemart.com/scripts/">Matt's Script Archive,
        Inc.</a></font></tt></th></tr>
   </table>
  </center>
 </body>
</html>
(END ERROR HTML)
        }
    }

    elsif ($error eq 'request_method') {
            print <<"(END ERROR HTML)";
Content-type: text/html

<html>
 <head>
  <title>Error: Request Method</title>
 </head>
 <body bgcolor=#FFFFFF text=#000000>
  <center>
   <table border=0 width=600 bgcolor=#9C9C9C>
    <tr><th><font size=+2>Error: Request Method</font></th></tr>
   </table>
   <table border=0 width=600 bgcolor=#CFCFCF>
    <tr><td>The Request Method of the Form you submitted did not match
     either <tt>GET</tt> or <tt>POST</tt>.  Please check the form and make sure the
     <tt>method=</tt> statement is in upper case and matches <tt>GET</tt> or <tt>POST</tt>.<p>

     <center><font size=-1>
      <a href="http://www.worldwidemart.com/scripts/formmail.shtml">FormMail</a> V1.9 &copy; 1995 - 2001  Matt Wright<br>
      A Free Product of <a href="http://www.worldwidemart.com/scripts/">Matt's Script Archive, Inc.</a>
     </font></center>
    </td></tr>
   </table>
  </center>
 </body>
</html>
(END ERROR HTML)
    }

    elsif ($error eq 'no_recipient') {
            print <<"(END ERROR HTML)";
Content-type: text/html

<html>
 <head>
  <title>Error: Bad/No Recipient</title>
 </head>
 <body bgcolor=#FFFFFF text=#000000>
  <center>
   <table border=0 width=600 bgcolor=#9C9C9C>
    <tr><th><font size=+2>Error: Bad/No Recipient</font></th></tr>
   </table>
   <table border=0 width=600 bgcolor=#CFCFCF>
    <tr><td>There was no recipient or an invalid recipient specified in the data sent to FormMail.  Please
     make sure you have filled in the <tt>recipient</tt> form field with an e-mail
     address that has been configured in <tt>\@recipients</tt>.  More information on filling in <tt>recipient</tt> form fields and variables can be
     found in the README file.<hr size=1>

     <center><font size=-1>
      <a href="http://www.worldwidemart.com/scripts/formmail.shtml">FormMail</a> V1.9 &copy; 1995 - 2001  Matt Wright<br>
      A Free Product of <a href="http://www.worldwidemart.com/scripts/">Matt's Script Archive, Inc.</a>
     </font></center>
    </td></tr>
   </table>
  </center>
 </body>
</html>
(END ERROR HTML)
    }

    elsif ($error eq 'missing_fields') {
        if ($Config{'missing_fields_redirect'}) {
            print "Location: $Config{'missing_fields_redirect'}\n\n";
        }
        else {
            foreach $missing_field (@error_fields) {
                $missing_field_list .= "      <li>$missing_field\n";
            }

            print <<"(END ERROR HTML)";
Content-type: text/html

<html>
 <head>
  <title>Error: Blank Fields</title>
 </head>
  <center>
   <table border=0 width=600 bgcolor=#9C9C9C>
    <tr><th><font size=+2>Error: Blank Fields</font></th></tr>
   </table>
   <table border=0 width=600 bgcolor=#CFCFCF>
    <tr><td>The following fields were left blank in your submission form:<p>
     <ul>
$missing_field_list
     </ul><br>

     These fields must be filled in before you can successfully submit the form.<p>
     Please use your browser's back button to return to the form and try again.<hr size=1>
     <center><font size=-1>
     </font></center>
    </td></tr>
   </table>
  </center>
 </body>
</html>
(END ERROR HTML)
        }
    }
    exit;
}

