;-------------------------------------------------------------
;
; The MedCommons Router Installer Script
;
; $Author:$
; $Id:$
;
;-------------------------------------------------------------

;--------------------------------
;Include Modern UI

!include "MUI.nsh"

;--------------------------------
;Configuration

  ;General
  Name "MedCommons Router"
  OutFile "mcrouter.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\MedCommons Router"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\MedCommons Router" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "MedCommons Router Software" SecRouter

  SetOutPath "$INSTDIR"
  
  File /r ..\..\build\installer\*.*

  CreateDirectory "$SMPROGRAMS\MedCommons Router"

  ;Store installation folder
  WriteRegStr HKCU "Software\MedCommons Router" "" $INSTDIR

  ; Create the shortcut to run jboss
  SetOutPath "$INSTDIR\jboss"
  CreateShortCut "$STARTMENU\Programs\MedCommons Router\Start MedCommons Router.lnk" "$INSTDIR\jboss\bin\run.bat" "-c router"
  SetOutPath "$INSTDIR"
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortCut "$STARTMENU\Programs\MedCommons Router\Uninstall MedCommons Router.lnk" "$INSTDIR\Uninstall.exe"

  ; Registry keys for uninstall
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Router" "DisplayName" "MedCommons Router (remove only)"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Router" "UninstallString" '"$INSTDIR\Uninstall.exe"'
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SEC_MEDCOMMONS_ROUTER ${LANG_ENGLISH} "MedCommons Router Software"
 
  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecRouter} $(DESC_SEC_MEDCOMMONS_ROUTER)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"
  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\MedCommons Router"

  DeleteRegKey /ifempty HKCU "Software\MedCommons Router"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Router" 
SectionEnd

