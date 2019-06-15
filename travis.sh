#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn -e clean install
  ;;

sonar)
  if [[ "$TRAVIS_PULL_REQUEST" =~ ^([0-9]*)$ ]]; then exit "0"; fi
  mvn clean \
    org.jacoco:jacoco-maven-plugin:prepare-agent \
    package \
    sonar:sonar \
    -Dsonar.projectKey=checkstyle_sonar-checkstyle \
    -Dsonar.organization=checkstyle \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.login="$SONAR_TOKEN"
  ;;

nondex)
  mvn --fail-never clean nondex:nondex -Dcheckstyle.skip=true
  cat `grep -RlE 'td class=.x' .nondex/ | cat` < /dev/null > output.txt
  RESULT="$(cat output.txt | wc -c)"
  cat output.txt
  echo 'Size of output:'${RESULT}
  if [[ "$RESULT" != 0 ]]; then false; fi
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit "1"
  ;;

esac
