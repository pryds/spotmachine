!define PROGRAMNAME "SpotMachine"
!define PROGRAMVERSION "0.2"
!define PROGRAMPUB "Pryds Software"

Name "${PROGRAMNAME} ${PROGRAMVERSION}"
OutFile "../spotmachine-${PROGRAMVERSION}-setup.exe"
InstallDir "$PROGRAMFILES\SpotMachine"
BrandingText "${PROGRAMPUB}"
ShowInstDetails hide #|show|nevershow
ShowUninstDetails hide #|show|nevershow
#UninstallIcon ""
XPStyle on

Page license
LicenseData "COPYING"
Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

Section
    SetOutPath $INSTDIR
    file "NEWS"
    file "INSTALL"
    file "COPYING"
    file "strings.list"
    file "strings*.properties"

    SetOutPath "$INSTDIR\gui"
    file "gui\*.class"

    SetOutPath "$INSTDIR\main"
    file "main\*.class"

    SetOutPath "$INSTDIR\resources"
    file "resources\*"

    SetOutPath "$INSTDIR"
    # CreateShortCut link.lnk target.file [parameters [icon.file [icon_index_number [start_options [keyboard_shortcut [description]]]]]]
    CreateShortCut "$SMPROGRAMS\${PROGRAMNAME}.lnk" "java" "main.SpotMachine" "" "" SW_SHOWNORMAL "" "Audio spot handling"

    WriteUninstaller "$INSTDIR\uninstall.exe"

    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "DisplayName" "${PROGRAMNAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "DisplayVersion" "${PROGRAMVERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
    #WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "DisplayIcon" ""
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "Publisher" "${PROGRAMPUB}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "URLInfoAbout" "http://pryds.eu/spotmachine"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "URLUpdateInfo" "http://pryds.eu/spotmachine"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "HelpLink" "http://pryds.eu/spotmachine"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "NoModify" "1"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine" "NoRepair" "1"
SectionEnd

Section "Uninstall"
    RMDir /r "$INSTDIR\gui"
    RMDir /r "$INSTDIR\main"
    RMDir /r "$INSTDIR\resources"
    Delete "$INSTDIR\NEWS"
    Delete "$INSTDIR\INSTALL"
    Delete "$INSTDIR\COPYING"
    Delete "$INSTDIR\strings.list"
    Delete "$INSTDIR\strings*.properties"
    Delete "$INSTDIR\uninstall.exe"
    # Do NOT use /r on the next line!
    RMDir "$INSTDIR"
    Delete "$SMPROGRAMS\${PROGRAMNAME}.lnk"
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\SpotMachine"
SectionEnd

