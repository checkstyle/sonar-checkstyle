#!/bin/bash

set -euo pipefail

case "$1" in

install)
  mvn -e --no-transfer-progress clean install
  ;;

integration-tests)
  SONAR_APP_VERSION="8.9.3.48735"
  if [[ ! -f ~/.m2/sonar-application-$SONAR_APP_VERSION.zip ]]; then
    URL="https://repox.jfrog.io/repox/sonarsource/org/sonarsource/sonarqube/"
    URL=$URL"sonar-application/$SONAR_APP_VERSION/sonar-application-$SONAR_APP_VERSION.zip"
    wget $URL -O ~/.m2/sonar-application-$SONAR_APP_VERSION.zip
  fi
  mkdir -p target/temp-downloads
  cp ~/.m2/sonar-application-$SONAR_APP_VERSION.zip target/temp-downloads/
  mvn -e --no-transfer-progress integration-test -DskipITs=false
  ;;

nondex)
  mvn -e --no-transfer-progress --fail-never clean nondex:nondex -Dcheckstyle.ant.skip=true
  cat `grep -RlE 'td class=.x' .nondex/ | cat` < /dev/null > output.txt
  RESULT="$(cat output.txt | wc -c)"
  cat output.txt
  echo 'Size of output:'${RESULT}
  if [[ "$RESULT" != 0 ]]; then false; fi
  ;;

*)
  echo "Unexpected mode: $1"
  exit "1"
  ;;

esac
