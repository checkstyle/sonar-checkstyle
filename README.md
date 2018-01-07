[![][travis img]][travis]
[![][wercker img]][wercker]
[![][teamcity img]][teamcity]
[![][sonar img]][sonar]

Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Checkstyle Plugin|Sonar|Checkstyle|Jdk
-----------------|-----|----------|---
4.7|5.6.6|8.7|1.8
4.6|5.6.6|8.6|1.8
4.5|5.6.6|8.5|1.8
4.4|5.6.6|8.4|1.8
4.3|5.6.6|8.3|1.8
4.2|5.6.6|8.2|1.8
4.1|5.6.6|8.1|1.8
4.0|5.6.6|8.0|1.8
3.8|5.6.6|7.8.2|1.8
3.7|5.6.6|7.7|1.8
3.6.1|5.6.6|7.6.1|1.8
3.6|5.6.4|7.6|1.8
3.5.1|5.6.4|7.5.1|1.8
3.5|5.6.4|7.5|1.8
3.4|5.6.4|7.4|1.8
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
[teamcity img]:https://img.shields.io/teamcity/http/teamcity.jetbrains.com/s/Checkstyle_SonarCheckstyleIdeaInspectionsMaster.svg?label=TeamCity%20inspections

[sonar]:https://sonarcloud.io/dashboard?id=com.github.checkstyle%3Acheckstyle-sonar-plugin-parent
[sonar img]:https://sonarcloud.io/api/badges/measure?key=com.github.checkstyle:checkstyle-sonar-plugin-parent&metric=sqale_debt_ratio

[wercker]: https://app.wercker.com/project/bykey/ece513d8a6eb70207dd3b805b63e8d1c
[wercker img]: https://app.wercker.com/status/ece513d8a6eb70207dd3b805b63e8d1c/s/master
