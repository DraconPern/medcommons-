;-------------------------------------------------------------
;
; The MedCommons Sample Data Installer Script
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
  Name "MedCommons Sample Data"
  OutFile "mcsampledata.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\MedCommons"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\MedCommons Sample Data" ""

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

Section "MedCommons Sample Data Software" SecRouter

  SetOutPath "$INSTDIR\Router\server\router\data\images"
  
  File /r sample_data\*.*

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\UninstallSampleData.exe"
  CreateShortCut "$STARTMENU\Programs\MedCommons Router\Uninstall Sample Data.lnk" "$INSTDIR\Uninstall.exe"

  ; Registry keys for uninstall
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Sample Data" "DisplayName" "MedCommons Sample Data (remove only)"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Sample Data" "UninstallString" '"$INSTDIR\Uninstall.exe"'
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SEC_MEDCOMMONS_ROUTER ${LANG_ENGLISH} "MedCommons Sample Data"
 
  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecRouter} $(DESC_SEC_MEDCOMMONS_ROUTER)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\UninstallSampleData.exe"
  Delete "$INSTDIR\server\router\data\images\ff51c4c25b2a35e0b7bfdd484df49b1f"
  RMDir /r "$INSTDIR\server\router\data\images\1.2.124.113932.1.170.223.162.178.20020510.165125.6461694"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons Sample Data" 
SectionEnd

