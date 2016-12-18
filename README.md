[![][travis img]][travis]

Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Checkstyle Plugin|2.0|2.1.1|2.2|2.3|2.4|3.1
-----------------|---|---|---|---|---|---
Sonar|3.6|3.6|4.5.1|4.5.1|4.5.2|4.5.2
Checkstyle|5.6|5.6|6.1|6.4.1|6.12.1|7.1
Jdk|1.6|1.6|1.6|1.7|1.7|1.8

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

