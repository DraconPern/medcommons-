from distutils.core import setup
import py2app

setup(
    plugin = ['MedCommons.py'],
    options=dict(py2app=dict(extension='.plugin', plist='Info.plist'))
    )
