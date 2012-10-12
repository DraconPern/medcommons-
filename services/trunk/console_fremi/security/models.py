from django.db import models

class Certificate(models.Model):

   issued = models.DateTimeField()

   CN = models.CharField(max_length=64)
   C  = models.CharField(max_length=2)
   ST = models.CharField(max_length=64)
   L  = models.CharField(max_length=64)
   O  = models.CharField(max_length=64)
   OU = models.CharField(max_length=64, blank=True)

   key = models.TextField(max_length=2048)
   csr = models.TextField(max_length=2048)
   crt = models.TextField(max_length=2048, null=True)
