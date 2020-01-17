#!/bin/bash
# Attention, there is no "-x" to avoid problems on Wercker
set -e

function checkout_from {
  CLONE_URL=$1
  PROJECT=$(echo "$CLONE_URL" | sed -nE 's/.*\/(.*).git/\1/p')
  mkdir -p .ci-temp
  cd .ci-temp
  if [ -d "$PROJECT" ]; then
    echo "Target project $PROJECT is already cloned, latest changes will be fetched"
    cd $PROJECT
    git fetch
    cd ../
  else
    for i in 1 2 3 4 5; do git clone $CLONE_URL && break || sleep 15; done
  fi
  cd ../
}

case $1 in

sonarqube)
  # token could be generated at https://sonarcloud.io/account/security/
  # executon on local:
  # SONAR_TOKEN=xxxxxx PR=xxxxxx WERCKER_GIT_BRANCH=xxxxxx ./.ci/wercker.sh sonarqube
  if [[ $PR && $PR =~ ^([0-9]*)$ ]]; then
      SONAR_PR_VARIABLES="-Dsonar.pullrequest.key=$PR"
      SONAR_PR_VARIABLES+=" -Dsonar.pullrequest.branch=$WERCKER_GIT_BRANCH"
      SONAR_PR_VARIABLES+=" -Dsonar.pullrequest.base=master"
      echo "SONAR_PR_VARIABLES: "$SONAR_PR_VARIABLES
  fi
  if [[ -z $SONAR_TOKEN ]]; then echo "SONAR_TOKEN is not set"; sleep 5s; exit 1; fi
  export MAVEN_OPTS='-Xmx2000m'
  mvn -e -Pno-validations clean package sonar:sonar $SONAR_PR_VARIABLES \
       -Dsonar.host.url=https://sonarcloud.io \
       -Dsonar.login=$SONAR_TOKEN \
       -Dsonar.projectKey=checkstyle_sonar-checkstyle \
       -Dsonar.organization=checkstyle
  echo "report-task.txt:"
  cat target/sonar/report-task.txt
  echo "Verification of sonar gate status"
  checkout_from https://github.com/viesure/blog-sonar-build-breaker.git
  sed -i'' "s|our.sonar.server|sonarcloud.io|" \
    .ci-temp/blog-sonar-build-breaker/sonar_break_build.sh
  export SONAR_API_TOKEN=$SONAR_TOKEN
  .ci-temp/blog-sonar-build-breaker/sonar_break_build.sh
  ;;

*)
  echo "Unexpected argument: $1"
  sleep 5s
  false
  ;;

esac
