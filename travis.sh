#!/bin/bash

set -euo pipefail

case "$1" in

install)
  mvn -e clean install
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
  echo "Unexpected mode: $1"
  exit "1"
  ;;

esac
