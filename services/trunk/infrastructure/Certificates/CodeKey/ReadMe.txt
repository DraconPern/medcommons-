The key and cert are properly installed in the keystore. The key alias is mc_kk. The key password and the keystore password are the new super-secret neither this nor that password. 

If this is refreshed, the keystore (and this text file) should be deployed (copied and checked into) $MC/router/components/dicomclient/trunk/CodeKey. Adjust the passwords in the copy before checking it in to adjust the store and key passwords. Use this to change the store password:

keytool -storepasswd -keystore mc_kkstore.jks

and this to change the key password:

keytool -keypasswd -alias mc_kk -keystore mc_kkstore.jks 

You'll be prompted for the old store and old store and key passwords in each case. Careful it's tricky. The password (assumed to be the same for both store and key) is plain text in $MC/router/components/dicomclient/trunk/pack.properites which should be adjusted if it (and/or keyfile name, alias, etc.) change. It's ``forge2010'' in the January 2010 version.
