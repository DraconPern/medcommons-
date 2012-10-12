#
# Makefile - generates docbook html (only) and it goes
# to the source directory instead of output.
#
# This is just a lightweight way to quickly see how the doc is going to look.
#
DBK=$(wildcard documents/SoftwareSpecifications/*.xml)
HTML=$(DBK:.xml=.html)

documents/SoftwareSpecifications/%.html: documents/SoftwareSpecifications/%.xml
	xsltproc TransformMedCommonsToDocbook.xsl $< | xsltproc DocBook/docbook-xsl-1.68.1/html/docbook.xsl - > $@
	cp $@ output/html/$*.html
	
all: $(HTML)
	
	
