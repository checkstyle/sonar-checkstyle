[![][travis img]][travis]
[![][teamcity img]][teamcity]

Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Checkstyle Plugin|Sonar|Checkstyle|Jdk
-----------------|-----|----------|---
3.4-SNAPSHOT|5.6.4|7.4|1.8
3.3|5.6.4|7.3|1.8
3.2|5.6.4|7.2|1.8
3.1.2|5.6.4|7.1.2|1.8
3.1.1|5.6.4|7.1.1|1.8
3.1|5.6.4|7.1|1.8
2.4|4.5.2|6.12.1|1.7
2.3|4.5.1|6.4.1|1.7
2.2|4.5.1|6.1|1.6
2.1.1|3.6|5.6|1.6
2|3.6|5.6|1.6


Jdk version depends on checkstyle's and sonar's jdk version:

checkstyle:6.0 use jdk6, checkstyle:6.2 use jdk7, checkstyle:7.0 use jdk8.

sonar:3.6 used jdk6, sonar:4.5 use jdk6, sonar:5.6-latest use jdk8.

## Usage
Dowload latest or required version from https://github.com/checkstyle/sonar-checkstyle/releases

Place it your sonar instance to "[YOUR_SONAR_PATH]/extensions/plugins", restart Sonar.

In the quality profile, activate some rules from Checkstyle and run an analysis on your project.
![checkstlye rules in sonar](https://github.com/checkstyle/resources/raw/master/img/sonar-wiki/sonar-in-docker.PNG)

[travis]:https://travis-ci.org/checkstyle/sonar-checkstyle/builds
[travis img]:https://secure.travis-ci.org/checkstyle/sonar-checkstyle.png

[teamcity]:https://teamcity.jetbrains.com/viewType.html?buildTypeId=Checkstyle_SonarCheckstyleIdeaInspectionsMaster
[teamcity img]:https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/Checkstyle_SonarCheckstyleIdeaInspectionsMaster.svg?label=TeamCity%20inspections
