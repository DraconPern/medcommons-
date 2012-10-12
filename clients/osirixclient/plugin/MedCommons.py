#
#  MedCommons.py
#
#  Created by Donald Way on 7/27/06.
#  Copyright (c) 2006 MedCommons. All rights reserved.
#

ccrin = u'''\
<?xml version="1.0" encoding="UTF-8"?>
<ContinuityOfCareRecord xmlns="urn:astm-org:CCR" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="urn:astm-org:CCR CCR_20051109.xsd">
  <CCRDocumentObjectID>a2GT3rAKenPkSfNXasDHM_fFWh8=</CCRDocumentObjectID>
  <Language>
    <Text>English</Text>
  </Language>
  <Version>V1.0</Version>
  <DateTime>
    <ExactDateTime />
  </DateTime>
  <Patient>
    <ActorID>Patient1</ActorID>
  </Patient>
  <From>
    <ActorLink>
      <ActorID>From1</ActorID>
      <ActorRole>
        <Text>Physician</Text>
      </ActorRole>
    </ActorLink>
  </From>
  <To>
    <ActorLink>
      <ActorID>9885221012304889</ActorID>
    </ActorLink>
  </To>
  <Body />
  <Actors>
    <Actor>
      <ActorObjectID>AA0001</ActorObjectID>
      <InformationSystem>
        <Name>MedCommons</Name>
        <Type>Repository</Type>
        <Version>V1.0 BETA</Version>
      </InformationSystem>
      <Source>
        <Actor>
          <ActorID>AA0001</ActorID>
        </Actor>
      </Source>
    </Actor>
    <Actor>
      <ActorObjectID>Patient1</ActorObjectID>
      <Person>
        <Name>
          <CurrentName>
            <Family />
            <Given />
          </CurrentName>
        </Name>
        <DateOfBirth>
          <ApproximateDateTime>
            <Text>Unknown</Text>
          </ApproximateDateTime>
        </DateOfBirth>
      </Person>
      <Source>
        <Actor>
          <ActorID>AA0002</ActorID>
        </Actor>
      </Source>
    </Actor>
    <Actor>
      <ActorObjectID>To1</ActorObjectID>
      <Source>
        <Actor>
          <ActorID>AA0001</ActorID>
        </Actor>
      </Source>
    </Actor>
    <Actor>
      <ActorObjectID>From1</ActorObjectID>
      <Source>
        <Actor>
          <ActorID>AA0003</ActorID>
        </Actor>
      </Source>
    </Actor>
    <Actor>
      <ActorObjectID>9885221012304889</ActorObjectID>
      <InformationSystem>
        <Name>MedCommons Notification</Name>
        <Type>Repository</Type>
      </InformationSystem>
      <EMail>
        <Value></Value>
      </EMail>
      <Source>
        <Actor>
          <ActorID>9885221012304889</ActorID>
        </Actor>
      </Source>
    </Actor>
  </Actors>
<References>
  <Reference>
    <ReferenceObjectID />
    <Type>
      <Text>application/dicom</Text>
    </Type>
    <Source>
      <ActorID>AA0002</ActorID>
    </Source>
    <Locations>
      <Location>
        <Description>
          <ObjectAttribute>
            <Attribute>URL</Attribute>
            <AttributeValueFoobar>
              <Value></Value>
            </AttributeValueFoobar>
          </ObjectAttribute>
          <ObjectAttribute>
            <Attribute>DisplayName</Attribute>
            <AttributeValue>
              <Value></Value>
            </AttributeValue>
          </ObjectAttribute>
          <ObjectAttribute>
            <Attribute>Confirmed</Attribute>
            <AttributeValue>
              <Value>true</Value>
            </AttributeValue>
            <Code>
              <Value>Confirmed</Value>
              <CodingSystem>MedCommons</CodingSystem>
              <Version>1.0.0.5</Version>
            </Code>
          </ObjectAttribute>
        </Description>
      </Location>
    </Locations>
  </Reference>
</References>
</ContinuityOfCareRecord>
'''

from Foundation import *
from AppKit import *
import objc
import socket, string, sys, time, xml.dom.minidom

ReportPluginFilter = objc.lookUpClass('ReportPluginFilter')
BrowserController = objc.lookUpClass('BrowserController')

class MedCommons(ReportPluginFilter):
    # plugin interface
    def initPlugin(self):
        #alert('initPlugin')
        self.debug = False

    def filterImage_(self, menuName):
        try:
            browser = BrowserController.currentBrowser()
            objects = NSMutableArray.arrayWithCapacity_(0)
            browser.filesForDatabaseOutlineSelection_(objects)
            count = objects.count()
            if count == 0:
                alert('You must first select a series to send!')
            else:
                self.debug = menuName == 'Debug'
                object = objects[0]
                series = object.valueForKey_('series')
                self.study = series.valueForKey_('study')
                self.report()
        except:
            alert('Uncaught exception in filterImage.')     

    def createReportForStudy_(self, study):
        #alert('createReportForStudy')
        self.study = study
        self.report()
        return True

    def deleteReportForStudy_(self, study):
        #alert('deleteReportForStudy')
        return True

    def reportDateForStudy_(self, study):
        #alert('reportDateForStudy')
        pass

    def report_action_(self, study, action):
        alert(action)
        self.study = study
        if action == 'openReport':
            self.report()

    # internal methods

    def updateCCR(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if self.debug:
                    alert(node.nodeName)
                if node.nodeName == 'References':
                    protoref = node.getElementsByTagName('Reference')[0]
                    node.removeChild(protoref)
                    seriesArray = self.study.valueForKey_('series')
                    for series in seriesArray:
                        ref = protoref.cloneNode(True)
                        ref.series = series
                        node.appendChild(ref)
                elif node.nodeName == 'Reference':
                    self.series = node.series
                elif node.nodeName == 'ReferenceObjectID':
                    replaceChildren(node, [self.doc.createTextNode('REF%04d' % self.referenceObjectIndex)])
                    self.referenceObjectIndex += 1
                elif node.nodeName == 'AttributeValueFoobar':
                    imageArray = self.series.valueForKey_('images')
                    images = []
                    for image in imageArray:
                        value = self.doc.createElement('Value')
                        value.appendChild(self.doc.createTextNode(unicode(image.valueForKey_('completePath'))))
                        images.append(value)
                    images.sort(lambda a, b: cmp(getText(a), getText(b)))
                    replaceChildren(node, images)
                elif node.nodeName == 'ExactDateTime':
                    replaceChildren(node, [self.doc.createTextNode(time.strftime('%Y-%m-%dT%H:%M:%S%Z', time.localtime()))])
                elif node.nodeName == 'CurrentName':
                    self.name = unicode(self.study.valueForKey_('name'))
                elif node.nodeName == 'Family' and self.name:
                    s = string.split(self.name)[0]
                    if s:
                        replaceChildren(node, [self.doc.createTextNode(s)])
                elif node.nodeName == 'Given' and self.name:
                    s = string.join(string.split(self.name)[1:])
                    if s:
                        replaceChildren(node, [self.doc.createTextNode(s)])
                    self.name = None
                elif node.nodeName == 'DateOfBirth':
                    self.dob = unicode(self.study.valueForKey_('dateOfBirth'))
                elif node.nodeName == 'Text' and self.dob:
                    replaceChildren(node, [self.doc.createTextNode(self.dob)])
                    self.dob = None
                self.updateCCR(node)
            node = node.nextSibling

    def report(self):
        try:
            s = send('GET /hasFocus HTTP/1.1\r\nHostname: localhost:16092\r\n\r\n')
            if s != '200':
                alert('You must first set document focus via the MedCommons gateway.')
                return
            self.name = None
            self.dob = None
            self.referenceObjectIndex = 1
            self.doc = xml.dom.minidom.parseString(ccrin)
            self.updateCCR(self.doc.documentElement)
            ccrout = self.doc.toxml('utf-8')
            self.doc = None
            send('POST /reportccr HTTP/1.1\r\nHostname: localhost:16092\r\nContent-Type: text/xml\r\nContent-Length: %d\r\n\r\n%s' % (len(ccrout), ccrout))
        except:
            #alert(str(sys.exc_info()))
            alert('Uncaught exception in report.')

def replaceChildren(node, children):
    while node.firstChild:
        node.removeChild(node.firstChild)
    for child in children:
        node.appendChild(child)

def getText(node):
    text = ''
    child = node.firstChild
    while child:
        if child.nodeType == xml.dom.Node.TEXT_NODE:
            text = text + child.nodeValue
        elif child.nodeType == xml.dom.Node.ELEMENT_NODE:
            text = text + getText(child)
        child = child.nextSibling
    return text

def send(msg):
    sock = socket.socket()
    try:
        try:
            sock.connect(('localhost', 16092))
            sock.send(msg)
            s = ''
            while True:
                i = s.find('\r\n\r\n')
                if i == -1:
                    s += sock.recv(1024)
                else:
                    return s[9:12]
        except socket.timeout:
            alert('CXP Web Daemon not responding.')
    finally:
        sock.close()

def alert(s):
    NSRunAlertPanel('MedCommons', NSLocalizedString(s, 0), None, None, None)
