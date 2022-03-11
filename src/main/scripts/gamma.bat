@echo off
REM
REM Copyright (c) 2022 Antonio Freixas
REM
REM This program is free software: you can redistribute it and/or modify
REM it under the terms of the GNU General Public License as published by
REM the Free Software Foundation, either version 3 of the License, or
REM (at your option) any later version.
REM
REM This program is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU General Public License for more details.
REM
REM You should have received a copy of the GNU General Public License
REM along with this program.  If not, see <http://www.gnu.org/licenses/>.

set SCRIPT_DIR=%~dp0
set GAMMA_HOME=%SCRIPT_DIR%app

if not defined JAVA_HOME (
  echo "JAVA_HOME is not defined!"
  pause
  goto :eof
)
if "%JAVA_HOME:~-1%"=="\" SET JAVA_HOME=%JAVA_HOME:~0,-1%

if not defined JAVAFX_HOME (
  echo "JAVAFX_HOME is not defined!"
  pause
  goto :eof
)
if "%JAVAFX_HOME:~-1%"=="\" SET JAVAFX_HOME=%JAVAFX_HOME:~0,-1%

start "" /B "%JAVA_HOME%\bin\java.exe" ^
  -p "%JAVAFX_HOME%\lib" --add-modules=javafx.controls,javafx.fxml,javafx.swing,javafx.web ^
  -cp "%GAMMA_HOME%\gamma.jar;%GAMMA_HOME%\commons-cli.jar" ^
  org.freixas.gamma.Gamma ^
  %*






