#!/usr/bin/env python

from django.db import models

class IdentityProvider(models.Model):
    source_id = models.CharField(max_length = 40,
                                 db_index = True)

    name = models.CharField(max_length = 80)

    domain = models.CharField(max_length = 64,
                              null = True)

    logouturl = models.URLField(max_length = 128,
                                null = True)

    website = models.URLField(max_length = 64,
                              null = True)

    format = models.CharField(max_length = 64,
			      null = True)

    class Meta:
        db_table = 'identity_providers'
