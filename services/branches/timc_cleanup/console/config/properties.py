#!/usr/bin/env python

from django.conf import settings
import os.path

from django.db import connection, transaction

def get_properties():
    map = {}

    c = connection.cursor()
    c.execute("SELECT property, value FROM mcproperties")

    for property, value in c.fetchall():
        if property.startswith('ac'):
            map[property[2:]] = parse_property(value)

    return map

def get_idps():
    c = connection.cursor()
    c.execute("SELECT source_id, name, display_login FROM identity_providers")

    return [dict(source_id=x[0], name=x[1], display_login=x[2]) for x in c.fetchall()]

def parse_property(str):
    try:
        i = int(str)
        
        # SS: Problem: PHP integers overflow at 2^32 on 32 bit platforms, and this 
        # causes values written
        # without quotes to behave very strangely.  Most notably, it affects
        # MCIDs which end up being completely different by some bizarro PHP
        # magic that I don't understand.   Here we work around it by avoiding 
        # them being treated as integers, so that way they get written out
        # as quoted string values. 
        if i < 2147483648:
                return i
        else:
                return str
        
    except ValueError:
        return str
