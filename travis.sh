#!/bin/bash

set -euo pipefail

case "$TEST" in

ci)
  mvn verify -B -e -V
  ;;

package)
  mvn clean package
  ;;

*)
  echo "Unexpected TEST mode: $TEST"
  exit 1
  ;;

esac
