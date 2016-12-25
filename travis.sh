#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn clean verify -B -e -V
  ;;

nondex)
  cd checkstyle-sonar-plugin
  mvn --fail-never clean nondex:nondex -Dcheckstyle.skip=true
  cat `grep -RlE 'td class=.x' .nondex/ | cat` < /dev/null > output.txt
  RESULT=$(cat output.txt | wc -c)
  cat output.txt
  echo 'Size of output:'$RESULT
  if [[ $RESULT != 0 ]]; then false; fi
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
