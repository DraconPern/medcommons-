#!/usr/bin/env python
import sys,os,xmpp,time
conn = xmpp.Client('gmail.com')
conn.connect(server = ('talk.google.com',5223))
conn.auth('onemctest', 'medcommons', '')
conn.sendInitPresence()
msg = sys.stdin.read();
for recipient in sys.argv:
  conn.send( xmpp.Message(recipient, msg) )

