#!/bin/bash
set -e

source ./.ci/util.sh

# token could be generated at https://sonarcloud.io/account/security/
# execution on local for master:
# SONAR_TOKEN=xxxxxx ./.ci/sonarqube.sh
# execution on local for non-master:
# SONAR_TOKEN=xxxxxx PR_NUMBER=xxxxxx PR_BRANCH_NAME=xxxxxx ./.ci/sonarqube.sh
checkForVariable "SONAR_TOKEN"

if [[ $PR_NUMBER =~ ^([0-9]+)$ ]]; then
  SONAR_PR_VARIABLES="-Dsonar.pullrequest.key=$PR_NUMBER"
  SONAR_PR_VARIABLES+=" -Dsonar.pullrequest.branch=$PR_BRANCH_NAME"
  SONAR_PR_VARIABLES+=" -Dsonar.pullrequest.base=master"
  echo "SONAR_PR_VARIABLES: ""$SONAR_PR_VARIABLES"
fi

export MAVEN_OPTS='-Xmx2000m'

mvn -e --no-transfer-progress -Pno-validations clean package sonar:sonar \
 $SONAR_PR_VARIABLES \
 -Dsonar.host.url=https://sonarcloud.io \
 -Dsonar.login="$SONAR_TOKEN" \
 -Dsonar.projectKey=checkstyle_sonar-checkstyle \
 -Dsonar.organization=checkstyle

echo "report-task.txt:"
cat target/sonar/report-task.txt

echo "Verification of sonar gate status"
export SONAR_API_TOKEN=$SONAR_TOKEN
./.ci/sonar-break-build.sh
