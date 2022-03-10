#
# Copyright (c) 2022 Antonio Freixas
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

#!/usr/bin/env bash

set -x

SCRIPT_FILE="$(readlink -f "${BASH_SOURCE}")"
SCRIPT_DIR="$(dirname "${SCRIPT_FILE}")"
GAMMA_HOME="${SCRIPT_DIR}/app"

if [ -z "${JAVA_HOME+x}" ];
then
  echo "JAVA_HOME is not defined!"
  read -n1 -r -p "Press any key to exit..." key
  exit
fi
JAVA_HOME="${JAVA_HOME%/}"
JAVA_HOME="${JAVA_HOME%\\}"

if [ -z "${JAVAFX_HOME+x}" ];
then
  echo "JAVAFX_HOME is not defined!"
  read -n1 -r -p "Press any key to exit..." key
  exit
fi
JAVAFX_HOME="${JAVAFX_HOME%\lib}"
JAVAFX_HOME="${JAVAFX_HOME%/}"
JAVAFX_HOME="${JAVAFX_HOME%\\}"

shift 1

"${JAVA_HOME}/bin/java.exe" \
  --module-path "${JAVAFX_HOME%}/lib" \
  --add-modules=javafx.controls,javafx.fxml,javafx.swing,javafx.web \
  -classpath "${GAMMA_HOME}/gamma.jar;${GAMMA_HOME}/commons-cli.jar" \
  org.freixas.gamma.Gamma \
  "$@"






