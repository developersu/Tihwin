; Include Modern UI
!include "MUI.nsh"
Unicode true

; Name and file
!define APPNAME "Tihwin"
!define COMPANYNAME "Dmitry Isaenko"

; Version is passed from CI-script via makensis -DVERSIONMAJOR -DVERSIONMINOR
; (falls back to the values below when built without them)
!ifndef VERSIONMAJOR
  !define VERSIONMAJOR 0
!endif
!ifndef VERSIONMINOR
  !define VERSIONMINOR 0
!endif

; Output file base name
!ifndef FILEOUT
  !define FILEOUT "Installer"
!endif

Name "Tihwin"
OutFile "${FILEOUT}-${VERSIONMAJOR}.${VERSIONMINOR}.exe"

; Default installation folder
InstallDir "$PROGRAMFILES\${APPNAME}"

; Get installation folder from registry if available
InstallDirRegKey HKCU "Software\${APPNAME}" ""

; Request application privileges for Windows Vista
RequestExecutionLevel admin

!define MUI_ICON installer_logo.ico
!define MUI_UNICON uninstaller_logo.ico
; !define MUI_FINISHPAGE_NOAUTOCLOSE

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
; Interface Settings
;--------------------------------
!define MUI_ABORTWARNING

;--------------------------------
; Language Selection Dialog Settings
;--------------------------------
; Remember the installer language
!define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
!define MUI_LANGDLL_REGISTRY_KEY "Software\${APPNAME}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"

;--------------------------------
; Pages
;--------------------------------
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

;--------------------------------
; Languages
;--------------------------------
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro MUI_LANGUAGE "SpanishInternational"
!insertmacro MUI_LANGUAGE "Indonesian"
!insertmacro MUI_LANGUAGE "PortugueseBR"

; Language strings
LangString l10n_CreateShortcut ${LANG_ENGLISH} "Create Desktop Shortcut"
LangString l10n_CreateShortcut ${LANG_RUSSIAN} "Создать ярлык на Рабочем столе"
LangString l10n_CreateShortcut ${LANG_JAPANESE} "デスクトップにショートカットを作成する"
LangString l10n_CreateShortcut ${LANG_SPANISHINTERNATIONAL} "Crear un acceso directo en el escritorio"
LangString l10n_CreateShortcut ${LANG_INDONESIAN} "Buat pintuan di Desktop"
LangString l10n_CreateShortcut ${LANG_PORTUGUESEBR} "Criar Atalho no Desktop"

BrandingText "Tihwin v${VERSIONMAJOR}.${VERSIONMINOR}"

;--------------------------------
; Install Section
;--------------------------------
Section "Tihwin" Install

  SetOutPath "$INSTDIR"
  ; Bundled JRE (jlink image, mounted in CI as /assembly/jdk)
  file /r \assembly\jdk
  ; Windows launcher (built by jpackage, see pom.xml)
  file Tihwin.exe
  file logo.ico

  ; Registry information for add/remove programs
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName"     "${APPNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion"  "${VERSIONMAJOR}.${VERSIONMINOR}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayIcon"     "$INSTDIR\logo.ico"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher"       "${COMPANYNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMajor"    "${VERSIONMAJOR}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "VersionMinor"    "${VERSIONMINOR}"

  ; Start Menu
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\Tihwin.exe" "" "$INSTDIR\logo.ico"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Uninstall.lnk" "$INSTDIR\Uninstall.exe"

  ; Store installation folder
  WriteRegStr HKCU "Software\${APPNAME}" "" $INSTDIR

  ; Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

;--------------------------------
; Installer Functions
;--------------------------------
Function .onInit
  ; Set mandatory installation rule to section
  SectionSetFlags ${Install} 17
FunctionEnd

Function un.onInit
  !insertmacro MUI_UNGETLANGUAGE
FunctionEnd

Function CreateDesktopShortCut
  CreateShortcut "$DESKTOP\Tihwin.lnk" "$INSTDIR\Tihwin.exe"
FunctionEnd

;--------------------------------
; Uninstaller Section
;--------------------------------
Section "Uninstall"

  ; Start Menu
  Delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"
  Delete "$DESKTOP\Tihwin.lnk"
  RMDir  "$SMPROGRAMS\${APPNAME}"

  ; Delete installed files
  RMDir  /r "$INSTDIR\jdk"
  Delete "$INSTDIR\Tihwin.exe"
  Delete "$INSTDIR\logo.ico"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\${APPNAME}"
  ; Cleanup records stored for uninstaller from the registry
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"

SectionEnd

;--------------------------------
; Uninstaller Functions
;--------------------------------
