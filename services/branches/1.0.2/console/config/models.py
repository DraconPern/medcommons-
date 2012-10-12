from django.db import models

class MCProperty(models.Model):
    property = models.CharField(max_length=765,
                                primary_key = True)
    value = models.CharField(max_length=765)
    infourl = models.CharField(max_length=765)
    comment = models.CharField(max_length=765)

    class Meta:
        db_table = 'mcproperties'
        
class MCFeature(models.Model):
        id = models.AutoField('id', primary_key = True, db_column = 'mf_id')
        name = models.CharField(db_column='mf_name', blank = False, max_length=60)
        enabled = models.BooleanField(db_column='mf_enabled', blank = False)
        description = models.CharField(db_column='mf_description',blank=True, max_length=255)
        class Meta:
                db_table = 'mcfeatures'

def get_property(name):
    try:
        p = MCProperty.objects.get(property='ac' + name)
        return parse_property(p.value)
    except MCProperty.DoesNotExist:
        return None

def parse_property(value):
    try:
	return int(value)
    except ValueError:
	return value
