[![][travis img]][travis]
[![][wercker img]][wercker]
[![][circleci img]][circleci]
[![][teamcity img]][teamcity]
[![][sonar img]][sonar]

[![sonar-checkstyle vulnerabilities][snyk-sonar img]][snyk-sonar]

Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Compatibility matrix from Sonar team: https://docs.sonarqube.org/display/PLUG/Plugin+Version+Matrix

Compatibility matrix from checkstyle team:

Checkstyle Plugin|Sonar min|Sonar max|Checkstyle|Jdk
-----------------|---------|---------|----------|---
9.0.1|8.9  |8.9+|9.0.1|1.8
8.45.1|7.9  |7.9+|8.45.1|1.8
8.42|7.9  |7.9+|8.42|1.8
8.41.1|7.9  |7.9+|8.41.1|1.8
8.40|7.9  |7.9+|8.40|1.8
8.39|7.9  |7.9+|8.39|1.8
8.38|7.9  |7.9+|8.38|1.8
8.37|7.9  |7.9+|8.37|1.8
8.35|7.9  |7.9+|8.35|1.8
4.34|7.9  |7.9+|8.34|1.8
4.33|7.9  |7.9+|8.33|1.8
4.32|7.9  |7.9+|8.32|1.8
4.31|7.9  |7.9+|8.31|1.8
4.30|7.9  |7.9+|8.30|1.8
4.29|7.9  |7.9+|8.29|1.8
4.28|7.9  |7.9+|8.28|11
4.27|6.7  |7.7+|8.27|1.8
4.26|6.7  |7.7+|8.26|1.8
4.25|6.7  |7.7+|8.25|1.8
4.24|6.7  |7.7+|8.24|1.8
4.23|6.7  |7.7+|8.23|1.8
4.22|6.7  |7.7+|8.22|1.8
4.21|6.7  |7.7+|8.21|1.8
4.20|6.7  |7.7+|8.20|1.8
4.19|6.7  |7.7+|8.19|1.8
4.18|6.7  |7.7+|8.18|1.8
4.17|6.7  |7.5|8.17|1.8
4.16|5.6.6|7.2|8.16|1.8
4.15|5.6.6|7.2|8.15|1.8
4.14|5.6.6|7.2|8.14|1.8
4.13|5.6.6|7.2|8.13|1.8
4.12|5.6.6|7.2|8.12|1.8
4.11|5.6.6|7.2|8.11|1.8
4.10.1|5.6.6|7.2|8.10.1|1.8
4.10|5.6.6|7.2|8.10|1.8
4.9|5.6.6|7.2|8.9|1.8
4.8|5.6.6|7.2|8.8|1.8
4.7|5.6.6|7.2|8.7|1.8
4.6|5.6.6|7.2|8.6|1.8
4.5|5.6.6|7.2|8.5|1.8
4.4|5.6.6|7.2|8.4|1.8
4.3|5.6.6|7.2|8.3|1.8
4.2|5.6.6|7.2|8.2|1.8
4.1|5.6.6|7.2|8.1|1.8
4.0|5.6.6|7.2|8.0|1.8
3.8|5.6.6|7.2|7.8.2|1.8
3.7|5.6.6|7.2|7.7|1.8
3.6.1|5.6.6|7.2|7.6.1|1.8
3.6|5.6.4|7.2|7.6|1.8
3.5.1|5.6.4|7.2|7.5.1|1.8
3.5|5.6.4|7.2|7.5|1.8
3.4|5.6.4|7.2|7.4|1.8
3.3|5.6.4|7.2|7.3|1.8
3.2|5.6.4|7.2|7.2|1.8
3.1.2|5.6.4|7.2|7.1.2|1.8
3.1.1|5.6.4|7.2|7.1.1|1.8
3.1|5.6.4|--|7.1|1.8
2.4|4.5.2|--|6.12.1|1.7
2.3|4.5.1|--|6.4.1|1.7
2.2|4.5.1|--|6.1|1.6
2.1.1|3.6|--|5.6|1.6
2|3.6|--|5.6|1.6


Jdk version depends on checkstyle's and sonar's jdk version:

checkstyle:6.0 use jdk6, checkstyle:6.2 use jdk7, checkstyle:7.0 use jdk8.

sonar:3.6 used jdk6, sonar:4.5 use jdk6, sonar:5.6-7.8 use jdk8, sonar:7.9-latest use jdk11.

## Usage
Install it from Sonar Update Center:
![Sonar Update Center](https://cloud.githubusercontent.com/assets/812984/23023964/e850b208-f40c-11e6-9577-a8e449de7e1d.png)

or do it manually:
Dowload latest or required version from https://github.com/checkstyle/sonar-checkstyle/releases .
Place jar file to your sonar instance to "[YOUR_SONAR_PATH]/extensions/plugins", restart Sonar.

In the quality profile, activate some rules from Checkstyle and run an analysis on your project.
![checkstlye rules in sonar](https://github.com/checkstyle/resources/raw/master/img/sonar-wiki/sonar-in-docker.PNG)

[travis]:https://travis-ci.org/checkstyle/sonar-checkstyle/builds
[travis img]:https://secure.travis-ci.org/checkstyle/sonar-checkstyle.png

[teamcity]:https://teamcity.jetbrains.com/viewType.html?buildTypeId=Checkstyle_SonarCheckstyleIdeaInspectionsMaster
[teamcity img]:https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Checkstyle_SonarCheckstyleIdeaInspectionsMaster)/statusIcon

[sonar]:https://sonarcloud.io/dashboard?id=checkstyle_sonar-checkstyle
[sonar img]:https://sonarcloud.io/api/project_badges/measure?project=checkstyle_sonar-checkstyle&metric=alert_status

[wercker]: https://app.wercker.com/project/bykey/ece513d8a6eb70207dd3b805b63e8d1c
[wercker img]: https://app.wercker.com/status/ece513d8a6eb70207dd3b805b63e8d1c/s/master

[circleci]: https://circleci.com/gh/checkstyle/sonar-checkstyle/tree/master
[circleci img]: https://circleci.com/gh/checkstyle/sonar-checkstyle/tree/master.svg?style=svg

[snyk-sonar]: https://snyk.io/test/github/checkstyle/sonar-checkstyle?targetFile=pom.xml
[snyk-sonar img]: https://snyk.io/test/github/checkstyle/sonar-checkstyle/badge.svg?targetFile=pom.xml

