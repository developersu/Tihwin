;Include Modern UI
  !include "MUI.nsh"
  Unicode true
;Name and file

  !define APPNAME "Tihwin"
  !define COMPANYNAME "Dmitry Isaenko"
  !define VERSIONMAJOR 0
  !define VERSIONMINOR 0
  !define VERSIONBUILD 0

  Name "Tihwin"
  OutFile "Installer.exe"

;Default installation folder
   InstallDir "$PROGRAMFILES\${APPNAME}"

;Get installation folder from registry if available
	InstallDirRegKey HKCU "Software\${APPNAME}" ""

;Request application privileges for Windows Vista
	RequestExecutionLevel admin

	!define MUI_ICON installer_logo.ico
	!define MUI_UNICON uninstaller_logo.ico
;	!define MUI_FINISHPAGE_NOAUTOCLOSE

	!define MUI_WELCOMEFINISHPAGE_BITMAP "leftbar.bmp"
	!define MUI_UNWELCOMEFINISHPAGE_BITMAP "leftbar_uninstall.bmp"

	!define MUI_FINISHPAGE_LINK "Tihwin at GitHub"
	!define MUI_FINISHPAGE_LINK_LOCATION https://github.com/developersu/Tihwin/

	!define MUI_FINISHPAGE_RUN "$INSTDIR\Tihwin.exe"
	!define MUI_FINISHPAGE_SHOWREADME
	!define MUI_FINISHPAGE_SHOWREADME_TEXT $(l10n_CreateShortcut)
	!define MUI_FINISHPAGE_SHOWREADME_FUNCTION CreateDesktopShortCut
	!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
;--------------------------------
;Language Selection Dialog Settings

  ;Remember the installer language
  !define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
  !define MUI_LANGDLL_REGISTRY_KEY "Software\${APPNAME}"
  !define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"

;--------------------------------
;Pages
;!define MUI_HEADERIMAGE
;!define MUI_HEADERIMAGE_RIGHTi
;!define MUI_HEADERIMAGE_BITMAP "install_header.bmp"
;!define MUI_HEADERIMAGE_UNBITMAP "install_header.bmp"

	!insertmacro MUI_PAGE_WELCOME
	!insertmacro MUI_PAGE_LICENSE "license.txt"
	!insertmacro MUI_PAGE_DIRECTORY
	!insertmacro MUI_PAGE_INSTFILES
	!insertmacro MUI_PAGE_FINISH

	!insertmacro MUI_UNPAGE_CONFIRM
	!insertmacro MUI_UNPAGE_INSTFILES
	!insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages
	!insertmacro MUI_LANGUAGE "English"
	!insertmacro MUI_LANGUAGE "Russian"
	!insertmacro MUI_LANGUAGE "Japanese"

;Language strings
  LangString l10n_CreateShortcut ${LANG_ENGLISH} "Create Desktop Shortcut"
  LangString l10n_CreateShortcut ${LANG_RUSSIAN} "Создать ярлык на Рабочем столе"

;--------------------------------
Section "Tihwin" Install

  SetOutPath "$INSTDIR"
  file /r \assembly\jdk
  file Tihwin.exe
  file logo.ico

; Registry information for add/remove programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon" "$\"$INSTDIR\logo.ico$\""
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "$\"${COMPANYNAME}$\""

; Start Menu
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\Tihwin.exe" "" "$INSTDIR\logo.ico"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

  ;Store installation folder
  WriteRegStr HKCU "Software\${APPNAME}" "" $INSTDIR

  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd
;--------------------------------
;Installer Functions

Function .onInit
; set mandatory installation rule to section
    SectionSetFlags ${Install} 17
FunctionEnd

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
FunctionEnd


Function CreateDesktopShortCut
    CreateShortcut "$DESKTOP\Tihwin.lnk" "$INSTDIR\Tihwin.exe"
FunctionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

; Start Menu
  Delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"
  Delete "$DESKTOP\Tihwin.lnk"
  rmDir "$SMPROGRAMS\${APPNAME}"

;Delete installed files
  RMDir /r "$INSTDIR\jdk\*"
  Delete "$INSTDIR\Tihwin.exe"
  Delete "$INSTDIR\logo.ico"
  Delete "$SMPROGRAMS\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\${APPNAME}"
; Cleanup records stored for uninstaller from the registry
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"

SectionEnd
;--------------------------------
;Uninstaller Functions
