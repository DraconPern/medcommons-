import objc
from Foundation import *
from AppKit import *
from PyObjCTools import NibClassBuilder, AppHelper

import re, socket, sys, threading, time, traceback, xml.dom.minidom

NibClassBuilder.extractClasses("MainMenu")

KB = 1024
MB = 1024 * KB
GB = 1024 * MB
TB = 1024 * GB

def alert(s):
    NSRunAlertPanel('MedCommonsFilter', NSLocalizedString(s, 0), None, None, None)

# class defined in MainMenu.nib
class MeterView(NibClassBuilder.AutoBaseClass):
    # the actual base class is NSView
    def initWithFrame_(self, frame):
        self = super(MeterView, self).initWithFrame_(frame)
        self.values = [0]
        return self

    def drawRect_(self, rect):
        NSFrameRect(NSMakeRect(rect.origin.x, rect.origin.y, rect.size.width, 1))
        maxval = reduce(max, self.values)
        width = (rect.size.width + 1) / len(self.values) - 1
        height = rect.size.height
        x = rect.origin.x
        y = rect.origin.y
        for value in self.values:
            if maxval:
                height = (float(value) / maxval) * rect.size.height
            else:
                height = 0
            if height:
                r = NSMakeRect(x, y, width, height)
                self.fill.setFill()
                NSRectFill(r)
                self.stroke.setFill()
                NSFrameRect(r)
            x += width + 1

    def update(self, values):
        self.values = values
        #self.setNeedsDisplay_(True)
        self.display()

# class defined in MainMenu.nib
class RecvMeterView(NibClassBuilder.AutoBaseClass):
    # the actual base class is MeterView
    def initWithFrame_(self, frame):
        self = super(RecvMeterView, self).initWithFrame_(frame)
        self.fill = NSColor.colorWithDeviceRed_green_blue_alpha_(.4706, .7804, .4706, .5)
        self.stroke = NSColor.colorWithDeviceRed_green_blue_alpha_(.0078, .1451, .0078, 1.0)
        return self

# class defined in MainMenu.nib
class SendMeterView(NibClassBuilder.AutoBaseClass):
    # the actual base class is MeterView
    def initWithFrame_(self, frame):
        self = super(SendMeterView, self).initWithFrame_(frame)
        self.fill = NSColor.colorWithDeviceRed_green_blue_alpha_(.7804, .4706, .4706, .5)
        self.stroke = NSColor.colorWithDeviceRed_green_blue_alpha_(.1451, .0078, .0078, 1.0)
        return self

# class defined in MainMenu.nib
class Session(NibClassBuilder.AutoBaseClass):
    # the actual base class is NSObject
    pass

# class defined in MainMenu.nib
class SessionController(NibClassBuilder.AutoBaseClass):
    # the actual base class is NSObject
    # The following outlets are added to the class:
    # action
    # detail
    # log
    # progress
    # recvMeter
    # recvRate
    # sendMeter
    # sendRate
    # session
    # status

    def init(self):
        NSApp().activateIgnoringOtherApps_(True)
        self.last = status()
        self.cancelling = False
        self.debugfd = open('/Users/Shared/MedCommons/cxpsm.log', 'wb')
        thread(self).start()
        return self

    def cancel_(self, sender):
        self.cancelling = True
        sock = socket.socket()
        sock.connect(('localhost', 16092))
        sock.send('GET /cancel?job=%s HTTP/1.1\r\nHostname: localhost:16092\r\n\r\n' % job)
        time.sleep(0.33)
        sock.close()

    def update(self, status):
        try:
            if self.action:
                if status.action and status.tisteps and status.tnsteps and (
                    status.action != self.last.action or
                    status.tisteps != self.last.tisteps or
                    status.tnsteps != self.last.tnsteps):
                    self.action.setTitle_('%(action)s (Step %(tisteps)d of %(tnsteps)d)' % status.__dict__)
            if self.status:
                if status.error:
                    self.status.setStringValue_(status.error)
                elif self.cancelling:
                    self.status.setStringValue_('Cancelling...')
                elif status.status != self.last.status:
                    if status.status is None:
                        status.status = ''
                    self.status.setStringValue_(status.status)
            if self.detail:
                if status.detail != self.last.detail:
                    if status.detail is None:
                        status.detail = ''
                    self.detail.setStringValue_(status.detail)
            if self.sendRate:
                if status.sendRate != self.last.sendRate:
                    self.sendRate.setStringValue_(self.formatbps(float(status.sendRate)))
            if self.recvRate:
                if status.recvRate != self.last.recvRate:
                    self.recvRate.setStringValue_(self.formatbps(float(status.recvRate)))
            if self.progress:
                if status.error:
                    self.progress.setHidden_(True)
                elif status.tibytes is not None and status.tnbytes is not None:
                    self.progress.setIndeterminate_(False)
                    self.progress.setDoubleValue_(100.0 * (float(status.tibytes) / status.tnbytes))
                else:
                    self.progress.setIndeterminate_(True)
                    self.progress.animate_(self)
            if self.sendMeter:
                if status.tasend != self.last.tasend:
                    self.sendMeter.update(status.tasend)
            if self.recvMeter:
                if status.tarecv != self.last.tarecv:
                    self.recvMeter.update(status.tarecv)
            if self.log:
                text = self.log.textStorage()
                for item in status.log:
                    text.replaceCharactersInRange_withString_(NSMakeRange(text.length(), 0), item + '\n')
                if len(status.log):
                    self.log.scrollRangeToVisible_(NSMakeRange(text.length(), 0))
            self.last = status
            if status.complete:
                NSApp().terminate_(None)
        except:
            self.debugfd.write(traceback.print_exc())
            self.debugfd.flush()

    def formatbps(self, bps):
        if bps < 10 * KB:
            return '%4.2fK/s' % (bps / KB)
        elif bps < 100 * KB:
            return '%4.1fK/s' % (bps / KB)
        elif bps < 10 * MB:
            return '%4.2fM/s' % (bps / MB)
        elif bps < 100 * MB:
            return '%4.1fM/s' % (bps / MB)
        elif bps < 10 * GB:
            return '%4.2fG/s' % (bps / GB)
        elif bps < 100 * GB:
            return '%4.1fG/s' % (bps / GB)
        elif bps < 10 * TB:
            return '%4.2fT/s' % (bps / TB)
        elif bps < 100 * TB:
            return '%4.1fT/s' % (bps / TB)
        else:
            return '  ! 0  '

class status:
    boolattrs = ('complete',)
    strattrs = ('error', 'action', 'status', 'detail') 
    intattrs = ('tifiles', 'tnfiles', 'tibytes', 'tnbytes', 'tiseries', 'tnseries', 'tisteps', 'tnsteps')
    logattrs = ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL')

    def __init__(self, doc=None):
        for attr in status.boolattrs + status.strattrs + status.intattrs:
            setattr(self, attr, None)
        self.sendRate = self.recvRate = 0
        self.tarecv = []
        self.tasend = []
        self.log = []
        if doc:
            self.walk(doc.documentElement)
            doc.unlink()
        if self.error:
            self.tarecv = [0] * 10
            self.tasend = [0] * 10
            self.sendRate = self.recvRate = 0

    def text(self, node):
        s = ''
        child = node.firstChild
        while child:
            if child.nodeType == xml.dom.Node.TEXT_NODE:
                s = s + child.nodeValue
            elif child.nodeType == xml.dom.Node.ELEMENT_NODE:
                s = s + self.text(child)
            child = child.nextSibling
        return s

    def walk(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if node.nodeName in status.boolattrs:
                    setattr(self, node.nodeName, True)
                elif node.nodeName in status.strattrs:
                    setattr(self, node.nodeName, self.text(node))
                elif node.nodeName in status.intattrs:
                    setattr(self, node.nodeName, int(self.text(node)))
                elif node.nodeName in status.logattrs:
                    self.log.append(self.text(node))
                elif node.nodeName == 'tarecv':
                    n = int(self.text(node))
                    self.recvRate += n
                    self.tarecv.append(n)
                elif node.nodeName == 'tasend':
                    n = int(self.text(node))
                    self.sendRate += n
                    self.tasend.append(n)
                else:
                    self.walk(node)
            node = node.nextSibling

class thread(threading.Thread):
    def __init__(self, controller):
        threading.Thread.__init__(self)
        self.controller = controller

    def run(self):
        pool = NSAutoreleasePool.alloc().init()
        try:
            while True:
                sock = socket.socket()
                try:
                    sock.connect(('localhost', 16092))
                    sock.send('GET /progress?job=%s HTTP/1.1\r\nHostname: localhost:16092\r\n\r\n' % job)
                    s = u''
                    while True:
                        i = s.find('\r\n\r\n')
                        if i == -1:
                            s += sock.recv(1024)
                        else:
                            s = s[i + 4:]
                            break
                    while True:
                        i = s.find('</progress>')
                        if i == -1:
                            s += sock.recv(1024)
                        else:
                            break

                    s = status(xml.dom.minidom.parseString(s))
                    self.controller.performSelectorOnMainThread_withObject_waitUntilDone_('update', s, True)
                    #self.controller.update(s)
                    if s.error:
                        break
                finally:
                    sock.close()
                time.sleep(0.05)
        finally:
            del pool

if __name__ == "__main__":
    if len(sys.argv) > 1:
        job = sys.argv[1]
    else:
        job = ''
    AppHelper.runEventLoop()
 
