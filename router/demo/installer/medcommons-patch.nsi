;--------------------------------
; MedCommons Installer Script
;
;$Author:$
;
;--------------------------------

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;Configuration

  ;General
  Name "MedCommons Patch"
  Caption "MedCommons Upgrade Installer Version ${VERSION} (Revision #${REVISION})"
  OutFile "medcommons-patch.exe"

  ;Default installation folder
  InstallDir "C:\Program Files\MedCommons"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\MedCommons" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

  !define MUI_FINISHPAGE_RUN $INSTDIR\gui-prototype.exe
  !define MUI_FINISHPAGE_RUN_TEXT "Launch Connection Monitor and Router Now?"
  !define MUI_FINISHPAGE_RUN_PARAMETERS "-tray -router"
  !define MUI_FINISHPAGE_NOAUTOCLOSE
  !define MUI_FINISHPAGE_SHOWREADME $INSTDIR\Router\ReleaseNotes.txt
  !define MUI_FINISHPAGE_SHOWREADME_TEXT "View Release Notes"

;--------------------------------
;Pages

  !define MUI_WELCOMEPAGE_TEXT "Welcome to the MedCommons Patch Installer.\r\n\r\nPlease ensure before running this installer that you have have shut down any MedCommons Router running on your machine."

  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE "License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "MedCommons Client" SecClient

  SetOutPath "$INSTDIR"
  
  File .\bin\gui-prototype.exe
  File .\bin\studies.xml

  ;Store installation folder
  WriteRegStr HKCU "Software\MedCommons" "" $INSTDIR
SectionEnd


Section "MedCommons Router Software" SecRouter
  SetOutPath "$INSTDIR"

  ;Note - creating uninstaller is not necessary for a patch

  SetOutPath "$INSTDIR\Router"
  
  File /r ..\build\installer\*.*

  ;Store installation folder
  WriteRegStr HKCU "Software\MedCommons Router" "" $INSTDIR

  ; Create the shortcut to run jboss
  SetOutPath "$INSTDIR\Router\jboss"
  CreateShortCut "$STARTMENU\Programs\MedCommons\Start MedCommons Router.lnk" "$INSTDIR\Router\jboss\bin\run.bat" "-c router"
  SetOutPath "$INSTDIR"
SectionEnd


;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SEC_MEDCOMMONS_CLIENT ${LANG_ENGLISH} "MedCommons Client Software"
  LangString DESC_SEC_MEDCOMMONS_MONITOR ${LANG_ENGLISH} "Displays an icon in your system tray to tell you the status of your MedCommons connection"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecClient} $(DESC_SEC_MEDCOMMONS_CLIENT)
    !insertmacro MUI_DESCRIPTION_TEXT ${SecRouter} $(DESC_SEC_MEDCOMMONS_MONITOR)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

