#!/usr/bin/env python

import sys

src = open(sys.argv[1], 'rb')
dst = open(sys.argv[2], 'wb')
while True:
    c = src.read(1)
    if not c:
        break
    d = ord(c)
    if d < 0 or d > 0x7e:
        d = ord('.')
    elif d == 0x09 or d == 0x0a or d == 0x0d:
        pass
    elif d < 0x020:
        d = ord('.')
    dst.write(chr(d))
src.close()
dst.close()
