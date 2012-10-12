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
  Name "MedCommons"
  OutFile "medcommons.exe"

  ;Default installation folder
  InstallDir "C:\MedCommons"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\MedCommons" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_FINISHPAGE_RUN $INSTDIR\gui-prototype.exe
  !define MUI_FINISHPAGE_RUN_TEXT "Launch Connection Monitor and Router Now?"
  !define MUI_FINISHPAGE_RUN_PARAMETERS "-tray -router"
  !define MUI_FINISHPAGE_NOAUTOCLOSE

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "License.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "MedCommons Client" SecClient

  SetOutPath "$INSTDIR"
  
  ;ADD YOUR OWN STUFF HERE!
  
  Call IsDotNETInstalled
  Pop $0
  StrCmp $0 1 found.NETFramework no.NETFramework

no.NETFramework:
  MessageBox MB_OK "You do not have .NET Framework Installed." 

found.NETFramework:

  File ..\bin\Release\gui-prototype.exe
  File ..\bin\Release\studies.xml
  File ..\bin\Release\Interop.stdole.dll
  File ..\bin\Release\Common.dll
  File ..\bin\Release\AxInterop.SHDocVw.dll
  File ..\bin\Release\Interop.SHDocVw.dll
  File "c:\program files\microsoft.net\primary interop assemblies\microsoft.mshtml.dll"

  CreateDirectory "$SMPROGRAMS\MedCommons"
  CreateShortCut "$STARTMENU\Programs\MedCommons\MedCommons.lnk" "$INSTDIR\gui-prototype.exe"

  ;Store installation folder
  WriteRegStr HKCU "Software\MedCommons" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortCut "$STARTMENU\Programs\MedCommons\Uninstall MedCommons.lnk" "$INSTDIR\Uninstall.exe"

  ; Registry keys for uninstall
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons" "DisplayName" "MedCommons Client (remove only)"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons" "UninstallString" '"$INSTDIR\Uninstall.exe"'
SectionEnd


Section "Connection Monitor" SecMonitor

  SetOutPath "$INSTDIR"
  
  Call IsDotNETInstalled
  Pop $0
  StrCmp $0 1 found.NETFramework no.NETFramework

no.NETFramework:
  MessageBox MB_OK "You do not have .NET Framework Installed." 

found.NETFramework:

  ;File ..\..\..\..\router\RouterMonitor\RouterMonitor\bin\Release\RouterMonitor.exe

  CreateDirectory "$SMPROGRAMS\MedCommons"
  CreateShortCut "$STARTMENU\Programs\MedCommons\System Monitor.lnk" "$INSTDIR\gui-prototype.exe" "-tray"

  ;Store installation folder
  WriteRegStr HKCU "Software\MedCommons" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortCut "$STARTMENU\Programs\MedCommons\Uninstall MedCommons.lnk" "$INSTDIR\Uninstall.exe"

  ; Default configuration
  ;WriteRegStr HKEY_CURRENT_USER  "Software\MedCommons\Connections\MedCommons Central" "Host" "medcommons.org"
  ;WriteRegStr HKEY_CURRENT_USER  "Software\MedCommons\Connections\MedCommons Central" "Port" "9080"
  WriteRegStr HKEY_CURRENT_USER  "Software\MedCommons\Connections\Local Computer" "Host" "localhost"
  WriteRegStr HKEY_CURRENT_USER  "Software\MedCommons\Connections\Local Computer" "Port" "9080"

  ; Registry keys for uninstall
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons" "DisplayName" "MedCommons Software (remove only)"
  WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons" "UninstallString" '"$INSTDIR\Uninstall.exe"'
SectionEnd

Section "MedCommons Router Software" SecRouter
  SetOutPath "$INSTDIR"

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortCut "$STARTMENU\Programs\MedCommons\Uninstall MedCommons.lnk" "$INSTDIR\Uninstall.exe"

  SetOutPath "$INSTDIR\Router"
  
  File /r ..\..\..\..\..\router\static-demo\build\installer\*.*

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
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMonitor} $(DESC_SEC_MEDCOMMONS_MONITOR)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"
  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\MedCommons"

  DeleteRegKey /ifempty HKCU "Software\MedCommons"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MedCommons" 
SectionEnd

; IsDotNETInstalled
 ;
 ; Usage:
 ;   Call IsDotNETInstalled
 ;   Pop $0
 ;   StrCmp $0 1 found.NETFramework no.NETFramework

 Function IsDotNETInstalled
   Push $0
   Push $1
   Push $2
   Push $3
   Push $4

   ReadRegStr $4 HKEY_LOCAL_MACHINE \
     "Software\Microsoft\.NETFramework" "InstallRoot"
   # remove trailing back slash
   Push $4
   Exch $EXEDIR
   Exch $EXEDIR
   Pop $4
   # if the root directory doesn't exist .NET is not installed
   IfFileExists $4 0 noDotNET

   StrCpy $0 0

   EnumStart:

     EnumRegKey $2 HKEY_LOCAL_MACHINE \
       "Software\Microsoft\.NETFramework\Policy"  $0
     IntOp $0 $0 + 1
     StrCmp $2 "" noDotNET

     StrCpy $1 0

     EnumPolicy:

       EnumRegValue $3 HKEY_LOCAL_MACHINE \
         "Software\Microsoft\.NETFramework\Policy\$2" $1
       IntOp $1 $1 + 1
        StrCmp $3 "" EnumStart
         IfFileExists "$4\$2.$3" foundDotNET EnumPolicy

   noDotNET:
     StrCpy $0 0
     Goto done

   foundDotNET:
     StrCpy $0 1

   done:
     Pop $4
     Pop $3
     Pop $2
     Pop $1
     Exch $0
 FunctionEnd

