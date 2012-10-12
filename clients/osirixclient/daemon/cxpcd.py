#!/usr/bin/env python

"""\
MedCommons CXP Python Implementation
Copyright 2006 MedCommons Inc.
@author Donald Way, MedCommons Inc.
"""

osirixSelect = '''\
osascript -e 'with timeout of 3600 seconds
  tell application "OsiriX"
    activate
    SelectImageFile from "%s"
  end tell 
end timeout'
'''

osirixActivate = '''\
osascript -e 'with timeout of 3600 seconds
  tell application "OsiriX"
    activate
  end tell 
end timeout'
'''

firefox = '''\
osascript -e 'with timeout of 3600 seconds
  tell application "Firefox"
    activate
    Get URL "%s"
  end tell
end timeout'
'''

firefoxActivate = '''\
osascript -e 'with timeout of 3600 seconds
  tell application "Firefox"
    activate
  end tell
end timeout'
'''

import BaseHTTPServer, SimpleHTTPServer, SocketServer
import getopt, logging, os, os.path, random, sha, signal, socket, string, sys, threading, time, traceback, urllib, webbrowser, xml.dom.minidom
import ccr, cxp, mtom

baseDir = '/Users/Shared/MedCommons/'
if not os.path.exists(baseDir):
    baseDir = '/var/medcommons/'

if os.path.exists(baseDir):
    fhandler = logging.FileHandler(baseDir + 'cxpcd.log')
    fhandler.setLevel(logging.DEBUG)
else:
    fhandler = None

busy = False
loop = True

#random.seed(0)

jobs = {}

#class cxpcdhttpserver(SocketServer.ThreadingMixIn, BaseHTTPServer.HTTPServer):
class cxpcdhttpserver(BaseHTTPServer.HTTPServer):
    pass

class cxpcdhttprequesthandler(SimpleHTTPServer.SimpleHTTPRequestHandler):

    def __init__(self, req, addr, server):
        SimpleHTTPServer.SimpleHTTPRequestHandler.__init__(self, req, addr, server)
        self.extensions_map['.dcm'] = 'application/dicom'

    def getvars(self, dict, data):
        list = string.split(data, '&')
        for item in list:
            pair = string.split(item, '=')
            if len(pair) > 1:
                key = urllib.unquote_plus(pair[0])
                val = urllib.unquote_plus(pair[1])
                if key and val:
                    dict[key] = val

    def getcookies(self, dict):
        if 'Cookie' in self.headers:
            list = string.split(self.headers['Cookie'], ';')
            for item in list:
                pair = string.split(item, '=')
                key = string.strip(pair[0])
                val = string.strip(pair[1])
                if key and val:
                    dict[key] = val

    def getpage(self, dict):
        self.getcookies(dict)
        part = string.split(self.path, '?')
        path = filter(None, string.split(part[0], '/'))
        if len(part) > 1:
            self.getvars(dict, part[-1])

        if 'contents' in dict:
            contents = dict['contents']
            dict['contents'] = str(len(contents)) + ' chars'
            print dict
            dict['contents'] = contents
        else:
            print dict

        if len(path) == 0:
            return False
        elif hasattr(self, 'call_' + path[0]):
            getattr(self, 'call_' + path[0])(path, dict)
        else:
            return False
        return True

    def do_GET(self):
        if not self.getpage({}):
            try:
                SimpleHTTPServer.SimpleHTTPRequestHandler.do_GET(self)
            except socket.error:
                pass

    def do_POST(self):
        type = self.headers.getheader('content-type')
        if type == 'text/xml':
            pass
        elif type != 'application/x-www-form-urlencoded':
            self.send_error(400)
            return
        s = self.headers.getheader('content-length')
        if not s:
            self.send_error(400)
            return
        length = int(s)
        dict = {}
        s = ''
        while len(s) < length:
            s += self.rfile.read(length - len(s))
        data = s
        if type == 'text/xml':
            dict['contents'] = data
        else:
            self.getvars(dict, data)
        if not self.getpage(dict):
            self.send_error(404)

    def sendfile(self, path, dict):
        fd = open('../' + path, 'r')
        data = fd.read()
        fd.close()
        self.wfile.write(data % dict)

    def default(self, path, dict):
        self.send_response(200)
        self.end_headers()
        self.sendfile('default.html', dict)

    def call_progress(self, path, dict):
        self.send_response(200)
        self.end_headers()
        if 'job' in dict and dict['job'] in jobs:
            s = jobs[dict['job']].xml()
            self.wfile.write(s)
        else:
            self.send_error(400)

    def call_status(self, path, dict):
        self.send_response(200)
        self.end_headers()
        if 'job' in dict and dict['job'] in jobs:
            s = jobs[dict['job']].json()
            self.wfile.write(s)
        else:
            self.send_error(400)

    def call_cancel(self, path, dict):
        if 'job' in dict and dict['job'] in jobs:
            jobs[dict['job']].cancelled = True

    def call_quit(self, path, dict):
        global loop
        loop = False
        signal.alarm(1)

    def call_reportccr(self, path, dict):
        global busy
        if busy:
            return
        if 'storageId' in cxp.params:
            thread = reportthread(path, dict)
        elif 'accountId' in cxp.params:
            thread = postthread(path, dict)
        else:
            return
        busy = True
        self.send_response(200)
        self.end_headers()
        try:
            thread.start()
        except:
            busy = False

    def call_tnum(self, path, dict):
        global busy
        if busy:
            self.send_error(400)
            return
        try:
            busy = True
            thread = getthread(path, dict)
            self.send_response(200)
            self.end_headers()
            #self.wfile.write(stathtml % { 'job': thread.key })
            thread.start()
        finally:
            busy = False

    def call_setAccountFocus(self, path, dict):
        cxp.params['accountId'] = dict['accountId']
        cxp.params['storageId'] = None
        cxp.params['groupAccountId'] = dict['groupAccountId']
        cxp.params['host'] = dict['cxphost']
        cxp.params['port'] = int(dict['cxpport'])
        cxp.params['protocol'] = dict['cxpprotocol']
        cxp.params['path'] = dict['cxppath']
        cxp.params['auth'] = dict['auth']
        self.send_response(200)
        self.end_headers()
        #self.wfile.write('<html><head><title>setAccountFocus</title></head><body><p>setAccountFocus submitted.</p></body></html>')

    def call_setDocumentFocus(self, path, dict):
        cxp.params['documentGuid'] = dict['guid']
        cxp.params['accountId'] = None
        cxp.params['storageId'] = dict['storageId']
        cxp.params['host'] = dict['cxphost']
        cxp.params['port'] = int(dict['cxpport'])
        cxp.params['protocol'] = dict['cxpprotocol']
        cxp.params['path'] = dict['cxppath']
        os.system(osirixActivate)
        self.send_response(200)
        self.end_headers()
        #self.wfile.write(stathtml % { 'job': thread.key })

    def call_setAuthorizationContext(self, path, dict):
        cxp.params['accountId'] = dict['accountId']
        #self.send_response(200)
        #self.end_headers()
        #self.wfile.write('<html><head><title>setAuthorizationContext</title></head><body><p>setAuthorizationContext submitted.</p></body></html>')

    def call_clearAccountFocus(self, path, dict):
        cxp.params['accountId'] = None
        cxp.params['groupAccountId'] = None
        cxp.params['host'] = None
        cxp.params['port'] = None
        cxp.params['protocol'] = None
        cxp.params['path'] = None
        cxp.params['auth'] = None
        #self.send_response(200)
        #self.end_headers()
        #self.wfile.write('<html><head><title>clearAccountFocus</title></head><body><p>clearAccountFocus submitted.</p></body></html>')

    def call_clearDocumentFocus(self, path, dict):
        cxp.params['documentGuid'] = None
        cxp.params['storageId'] = None
        cxp.params['host'] = None
        cxp.params['port'] = None
        cxp.params['protocol'] = None
        cxp.params['path'] = None
        #self.send_response(200)
        #self.end_headers()
        #self.wfile.write('<html><head><title>clearDocumentFocus</title></head><body><p>clearDocumentFocus submitted.</p></body></html>')

    def call_clearAuthorizationContext(self, path, dict):
        cxp.params['accountId'] = None
        #self.send_response(200)
        #self.end_headers()
        #self.wfile.write ('<html><head><title>clearAuthorizationContext</title></head><body><p>clearAuthorizationContext submitted.</p></body></html>')

    def call_hasFocus(self, path, dict):
        if 'storageId' in cxp.params and cxp.params['storageId']:
            self.send_response(200)
        elif 'accountId' in cxp.params and cxp.params['accountId']:
            self.send_response(200)
        else:
            self.send_response(400)
        self.end_headers()

class cxpcdthread(threading.Thread):
    def __init__(self, path, dict):
        threading.Thread.__init__(self)
        self.path = path
        self.dict = dict
        self.key = str(random.randint(0, 0xffffffffffffffffl))
        self.setDaemon(True)

    def install(self, init=None):
        job = status('net.medcommons.cxpcd')
        job.logger.info('Begin session.')
        if init:
            job.update(init)
        jobs[self.key] = job
        return job

class postthread(cxpcdthread):
    def run(self):
        global busy
        params = cxp.params.copy()
        params['storageId'] = '-1'
        job = self.install({'action': 'Create CCR',
                            'status': 'Identifying DICOM',
                            'tisteps': 1,
                            'tnsteps': 2 })
        os.system('"/Library/Application Support/MedCommons/CXP Session Manager.app/Contents/MacOS/CXP Session Manager" %s &' % self.key)
        #os.system(firefoxActivate)
        try:
            try:
                doc = xml.dom.minidom.parseString(self.dict['contents'])
            except:
                fd = open('debugccr.xml', 'wb')
                fd.write(self.dict['contents'])
                fd.close()
                job.logger.error('Error in CCR generated by MedCommons.plugin, dumping XML to file "debugccr.xml"')
                job.error = 'Bad CCR'
                return
            study = ccr.study(doc).extract()
            job.detail = '%(family)s %(given)s %(dob)s' % study

            for series in study['elements']:
                a = []
                for element in series['elements']:
                    job.detail = element['path']
                    hash = sha.new()
                    fd = open(element['path'], 'rb')
                    try:
                        while True:
                            if job.cancelled:
                                raise mtom.cancel()
                            s = fd.read(0x1000)
                            if not s:
                                break
                            hash.update(s)
                    finally:
                        fd.close()
                    digest = hash.hexdigest()
                    element['sha1'] = element['guid'] = digest
                    a.append(digest)
                job.detail = None
                a.sort(lambda x, y: cmp(x, y))
                hash = sha.new()
                hash.update(reduce(lambda x, y: x + y, a))
                series['guid'] = hash.hexdigest()

		node = series['foobar']
		parent = node.parentNode
		parent.removeChild(node)
		node = doc.createElement('AttributeValue')
		parent.appendChild(node)
		parent = node
		node = doc.createElement('Value')
		parent.appendChild(node)
		parent = node
		node = doc.createTextNode('mcid://%s' % series['guid'])
		parent.appendChild(node)
                for element in series['elements']:
                    element['contentType'] = 'application/dicom'
                    element['parentName'] = series['guid']

            s = doc.toxml()
            hash = sha.new()
            hash.update(s)
            ccrfn = '%s' % hash.hexdigest()
            fd = open(ccrfn, 'wb')
            try:
                os.chmod(ccrfn, 0x1ff)
                fd.write(s)
            finally:
                fd.close()

            study['elements'][0:0] = [{ 'path': ccrfn,
                                        'contentType': 'application/x-ccr+xml', 
                                        'cid': random.randint(0, 0xffffffffffffffffffffffffffffffffl),
                                        'guid': ccrfn }]

            job.lock.acquire()
            job.tisteps = 2
            job.status = 'Uploading DICOM'
            job.lock.release()
            
            cxp.put(params, job).cxp(study)

            params['documentGuid'] = ccrfn
            s = '%(protocol)s://%(host)s:%(port)d/router/access?provision&g=%(documentGuid)s&a=%(accountId)s&at=%(auth)s&p=%(storageId)s' % params
            #webbrowser.open(s)
            os.system(firefox % s)
            job.complete = True
        except mtom.cancel:
            job.logger.critical('Session cancelled.')
            job.error = 'Session cancelled.'
            busy = False
        except:
            job.logger.exception('Uncaught exception.')
            job.error = 'Session failed.'
            busy = False
        else:
            busy = False

class reportthread(cxpcdthread):
    def run(self):
        global busy
        params = cxp.params.copy()
        job = self.install({'action': 'Add DICOM',
                            'status': 'Downloading CCR',
                            'tisteps': 1,
                            'tnsteps': 3 })
        os.system('"/Library/Application Support/MedCommons/CXP Session Manager.app/Contents/MacOS/CXP Session Manager" %s &' % self.key)
        #os.system(firefoxActivate)
        try:
            try:
                doc = xml.dom.minidom.parseString(self.dict['contents'])
            except:
                fd = open('debugccr.xml', 'wb')
                fd.write(self.dict['contents'])
                fd.close()
                job.logger.error('Error in CCR generated by MedCommons.plugin, dumping XML to file "debugccr.xml"')
                job.error = 'Bad CCR'
                return
            study = ccr.study(doc).extract()
            job.detail = '%(family)s %(given)s %(dob)s' % study

            cxp.get(params, job).cxp({ 'elements': [{ 'guid': params['documentGuid'] }]})

            doc = xml.dom.minidom.parse(params['documentGuid'])

            refs = doc.getElementsByTagName('References')
            if len(refs) == 1:
                refs = refs[0]
            else:
                refs = doc.createElement('References')
                doc.documentElement.appendChild(refs)

            job.lock.acquire()
            job.tisteps = 2
            job.status = 'Identifying DICOM'
            job.lock.release()

            for series in study['elements']:
                a = []
                for element in series['elements']:
                    job.detail = element['path']
                    hash = sha.new()
                    fd = open(element['path'], 'rb')
                    try:
                        while True:
                            if job.cancelled:
                                raise mtom.cancel()
                            s = fd.read(0x1000)
                            if not s:
                                break
                            hash.update(s)
                    finally:
                        fd.close()
                    digest = hash.hexdigest()
                    element['sha1'] = element['guid'] = digest
                    a.append(digest)
                job.detail = None
                a.sort(lambda x, y: cmp(x, y))
                hash = sha.new()
                hash.update(reduce(lambda x, y: x + y, a))
                series['guid'] = hash.hexdigest()

                ref = doc.createElement('Reference')
                refs.appendChild(ref)
                node = doc.createElement('ReferenceObjectID')
                ref.appendChild(node)
                node.appendChild(doc.createTextNode(''))
                node = doc.createElement('Type')
                ref.appendChild(node)
                child = doc.createElement('Text')
                node.appendChild(child)
                child.appendChild(doc.createTextNode('application/dicom'))
                node = doc.createElement('Source')
                ref.appendChild(node)
                child = doc.createElement('ActorID')
                node.appendChild(child)
                child.appendChild(doc.createTextNode('AA0002'))

                locs = doc.createElement('Locations')
                ref.appendChild(locs)
                loc = doc.createElement('Location')
                locs.appendChild(loc)
                desc = doc.createElement('Description')
                loc.appendChild(desc)

                node = doc.createElement('ObjectAttribute')
                desc.appendChild(node)
                child = doc.createElement('Attribute')
                node.appendChild(child)
                child.appendChild(doc.createTextNode('URL'))
                child = doc.createElement('AttributeValue')
                node.appendChild(child)
                value = doc.createElement('Value')
                child.appendChild(value)
                value.appendChild(doc.createTextNode('mcid://%s' % series['guid']))

                node = doc.createElement('ObjectAttribute')
                desc.appendChild(node)
                child = doc.createElement('Attribute')
                node.appendChild(child)
                child.appendChild(doc.createTextNode('DisplayName'))
                child = doc.createElement('AttributeValue')
                node.appendChild(child)
                value = doc.createElement('Value')
                child.appendChild(value)

                node = doc.createElement('ObjectAttribute')
                desc.appendChild(node)
                child = doc.createElement('Attribute')
                node.appendChild(child)
                child.appendChild(doc.createTextNode('Confirmed'))
                child = doc.createElement('AttributeValue')
                node.appendChild(child)
                value = doc.createElement('Value')
                child.appendChild(value)
                value.appendChild(doc.createTextNode('true'))
                child = doc.createElement('Code')
                node.appendChild(child)
                temp = doc.createElement('Value')
                child.appendChild(temp)
                temp = doc.createElement('CodingSystem')
                child.appendChild(temp)
                temp = doc.createElement('Version')
                child.appendChild(temp)

                for element in series['elements']:
                    element['contentType'] = 'application/dicom'
                    element['parentName'] = series['guid']

            s = doc.toxml()
            hash = sha.new()
            hash.update(s)
            ccrfn = '%s' % hash.hexdigest()
            fd = open(ccrfn, 'wb')
            try:
                os.chmod(ccrfn, 0x1ff)
                fd.write(s)
            finally:
                fd.close()

            study['elements'][0:0] = [{ 'path': ccrfn,
                                        'contentType': 'application/x-ccr+xml', 
                                        'cid': random.randint(0, 0xffffffffffffffffffffffffffffffffl),
                                        'guid': ccrfn }]

            job.lock.acquire()
            job.tisteps = 3
            job.status = 'Uploading DICOM'
            job.lock.release()
            
            cxp.put(params, job).cxp(study)

            params['documentGuid'] = ccrfn
            s = '%(protocol)s://%(host)s:%(port)d/router/access?g=%(documentGuid)s&a=%(storageId)s' % params
            #webbrowser.open(s)
            os.system(firefox % s)
            job.complete = True
        except mtom.cancel:
            job.logger.critical('Session cancelled.')
            job.error = 'Session cancelled.'
            busy = False
        except:
            job.logger.exception('Uncaught exception.')
            job.error = 'Session failed.'
            busy = False
        else:
            busy = False

class getthread(cxpcdthread):
    def run(self):
        params = cxp.params.copy()
        job = self.install({'action': 'Download DICOM', 
                            'status': 'Downloading CCR',
                            'tisteps': 1,
                            'tnsteps': 2 })
        os.system('"/Library/Application Support/MedCommons/CXP Session Manager.app/Contents/MacOS/CXP Session Manager" %s &' % self.key)

        try:
            study = { 'elements': [] }

            cxp.get(params, job).cxp({ 'elements': [{ 'guid': params['documentGuid'] }]})

            ccr = xml.dom.minidom.parse(params['documentGuid'])

            def walk(root, elements):
                node = root.firstChild
                while node:
                    if node.nodeType == xml.dom.Node.TEXT_NODE:
                        s = node.nodeValue.strip()
                        if s[:7] == 'mcid://':
                            elements.append({ 'guid': s[7:] })
                    elif node.nodeType == xml.dom.Node.ELEMENT_NODE:
                        walk(node, elements)
                    node = node.nextSibling

            job.lock.acquire()
            job.tisteps = 2
            job.status = 'Downloading DICOM'
            job.lock.release()

            walk(ccr.documentElement, study['elements'])
            cxp.get(params, job).cxp(study)
            for series in study['elements']:
                s = os.path.join(os.getcwd(), series['guid'])
                tt = []
                while True:
                    tup = os.path.split(s)
                    tt.insert(0, tup[1])
                    s = tup[0]
                    if s == '/':
                        break
                os.system(osirixSelect % ':'.join(tt))
            job.complete = True
        except mtom.cancel:
            job.logger.critical('Session cancelled.')
            job.error = 'Session cancelled.'
            busy = False
        except:
            job.logger.exception('Uncaught exception.')
            job.error = 'Session failed.'
            busy = False
        else:
            busy = False

class handler(logging.Handler):
    def __init__(self, status):
        logging.Handler.__init__(self)
        self.status = status
    def emit(self, rec):
        self.status.emit(('%(levelname)s' % rec.__dict__, self.format(rec)))

class status(cxp.status):
    def __init__(self, name):
        cxp.status.__init__(self, name)
        h = handler(self)
        h.setFormatter(logging.Formatter('%(message)s'))
        h.setLevel(logging.DEBUG)
        if fhandler:
            self.logger.addHandler(fhandler)
        self.logger.addHandler(h)
        self.logger.setLevel(logging.DEBUG)
        self.tisteps = self.tnsteps = None
        self.action = self.status = None        
        self.log = []

    def emit(self, tup):
        self.lock.acquire()
        try:
            self.log.append(tup)
        finally:
            self.lock.release()
    
    def xmlc(self, doc, *keys):
        for key in keys:
            if not hasattr(self, key):
                return
            if getattr(self, key) is None:
                return
        for key in keys:
            attr = getattr(self, key)
            if isinstance(attr, bool):
                doc.documentElement.appendChild(doc.createElement(key))
            elif isinstance(attr, list):
                for item in attr:
                    node = doc.createElement(key)
                    node.appendChild(doc.createTextNode(unicode(item)))
                    doc.documentElement.appendChild(node)
            else:
                node = doc.createElement(key)
                node.appendChild(doc.createTextNode(unicode(attr)))
                doc.documentElement.appendChild(node)

    def xml(self):
        self.lock.acquire()
        try:
            doc = xml.dom.minidom.parseString('<progress/>')
            self.xmlc(doc, 'error')
            self.xmlc(doc, 'complete')
            self.xmlc(doc, 'action')
            self.xmlc(doc, 'status')
            self.xmlc(doc, 'detail')
            self.xmlc(doc, 'tifiles', 'tnfiles')
            self.xmlc(doc, 'tibytes', 'tnbytes')
            self.xmlc(doc, 'tiseries', 'tnseries')
            self.xmlc(doc, 'tisteps', 'tnsteps')
            self.xmlc(doc, 'tarecv', 'tasend')
            for level, msg in self.log:
                node = doc.createElement(level)
                node.appendChild(doc.createTextNode(msg))
                doc.documentElement.appendChild(node)
            self.log = []
        finally:
            self.lock.release()
        s = doc.toxml()
        doc.unlink()
        return s

    def jsonc(self, *keys):
        for key in keys:
            if not hasattr(self, key):
                return
            if getattr(self, key) is None:
                return
        s = ''
        for key in keys:
            attr = getattr(self, key)
            if isinstance(attr, bool):
                s += '"%s": true, ' % key
            elif isinstance(attr, list):
                t = ''
                for item in attr:
                    t += '%s, ' % str(item)
                s += '"%s": [%s], ' % (key, t)
            else:
                s += '"%s": "%s", ' % (key, attr)
        return s

    def json(self):
        self.lock.acquire()
        try:
            s = '{'
            s += self.jsonc('error')
            s += self.jsonc('complete')
            s += self.jsonc('action')
            s += self.jsonc('status')
            s += self.jsonc('detail')
            s += self.jsonc('tifiles', 'tnfiles')
            s += self.jsonc('tibytes', 'tnbytes')
            s += self.jsonc('tiseries', 'tnseries')
            s += self.jsonc('tisteps', 'tnsteps')
            s += self.jsonc('tarecv', 'tasend')
            s += '['
            for level, msg in self.log:
                s += '"%s": "%s"' % (level, msg)
            s += ']}'
            self.log = []
            print s
            return s
        finally:
            self.lock.release()

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

shortopts = 'xio'
longopts = ['host=', 'port=', 'path=']

def kill(signum, stackframe):
    global loop
    loop = False
    signal.alarm(1)
    sys.exit(1)

def usage():
    s = ''
    for c in shortopts:
        s += '-%s ' % c
    for i in longopts:
        s += '--%s ' % i

def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], shortopts, longopts)
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    for o, a in opts:
        if o[:2] == '--':
            cxp.params[o[2:]] = a
        elif o[:1] == '-':
            cxp.params[o[1:]] = True

    #fd = open('stat.html', 'rb')
    #global stathtml
    #stathtml = fd.read()
    #fd.close()
    signal.signal(signal.SIGINT, kill)
    os.chdir(baseDir)
    httpd = cxpcdhttpserver(('', 16092), cxpcdhttprequesthandler)
    for key in cxp.params:
        print '%s: ' % key, cxp.params[key]
    print 'serving...'
    while loop:
        httpd.handle_request()

if __name__ == "__main__":
    main()
