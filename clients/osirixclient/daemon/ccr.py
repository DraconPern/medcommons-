
import xml.dom.minidom

class study:
    def __init__(self, doc):
        self.doc = doc
        self.study = { 'given': '', 'family': '', 'dob': '', 'elements': [] }

    def extract(self):
        self.walk(self.doc.documentElement)
        return self.study

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

    def walkName(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if node.nodeName == 'Family':
                    self.study['family'] = self.text(node)
                elif node.nodeName == 'Given':
                    self.study['given'] = self.text(node)
                else:
                    self.walkName(node)
            node = node.nextSibling

    def walkDateOfBirth(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if node.nodeName == 'Text':
                    self.study['dob'] = self.text(node)
                self.walkDateOfBirth(node)
            node = node.nextSibling

    def walkReferences(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if node.nodeName == 'AttributeValueFoobar':
                    self.study['elements'].append({ 'foobar': node, 'elements': [] })
                    values = node.getElementsByTagName('Value')
                    for value in values:
                        self.study['elements'][-1]['elements'].append({ 'path': self.text(value).strip() })
                else:
                    self.walkReferences(node)
            node = node.nextSibling

    def walk(self, root):
        node = root.firstChild
        while node:
            if node.nodeType == xml.dom.Node.ELEMENT_NODE:
                if node.nodeName == 'CurrentName':
                    self.walkName(node)
                elif node.nodeName == 'DateOfBirth':
                    self.walkDateOfBirth(node)
                elif node.nodeName == 'References':
                    self.walkReferences(node)
                else:
                    self.walk(node)
            node = node.nextSibling
