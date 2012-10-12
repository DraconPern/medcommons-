#!/bin/bash
./dnscurl.pl --debug 1 --keyname mc_prod -- -H "Content-Type: text/xml; charset=UTF-8" -X POST --upload-file ./myhealthespace.com-create_request.xml https://route53.amazonaws.com/2010-10-01/hostedzone/Z320MEKGXIPCN0/rrset
# ./dnscurl.pl --debug 1 --keyname mc_prod -- -H "Content-Type: text/xml; charset=UTF-8" -X POST --upload-file ./medcommons.net-create_request.xml https://route53.amazonaws.com/2010-10-01/hostedzone/Z12LQT0Y1DQ425/rrset
# ./dnscurl.pl --debug 1 --keyname mc_prod -- -H "Content-Type: text/xml; charset=UTF-8" -X POST --upload-file ./medcommons.net-create_request_1.xml https://route53.amazonaws.com/2010-10-01/hostedzone/Z12LQT0Y1DQ425/rrset
# ./dnscurl.pl --keyname mc_prod -- -H "Content-Type: text/xml; charset=UTF-8" -X POST --upload-file ./medcommons.net-create_request_0.xml https://route53.amazonaws.com/2010-10-01/hostedzone/Z12LQT0Y1DQ425/rrset
# ./dnscurl.pl --keyfile ~/.aws-secrets --keyname mc_prod -- -v -H "Content-Type: text/xml; charset=UTF-8" https://route53.amazonaws.com/2010-10-01/hostedzone
# ./dnscurl.pl --keyname mc_prod -- https://route53.amazonaws.com/2010-10-01/hostedzone/Z12LQT0Y1DQ425
#
