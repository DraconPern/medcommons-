from distutils.core import setup
import py2app

setup(
    app=['CXP Session Manager.py'],
    data_files=['MainMenu.nib'],
    options=dict(py2app=dict(iconfile='application.icns', plist='Info.plist'))
)
