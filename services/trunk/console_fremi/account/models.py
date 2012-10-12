from django.db import models

# Create your models here.


"""Tracks login success / failure to provide rate limiting for 
   logins.
"""
class LoginTracking(models.Model):
    userinput = models.CharField(max_length=255,
                                primary_key = True)
    failurecounter = models.IntegerField()
    lasttime = models.IntegerField()

    class Meta:
        db_table = 'logintrakker'