[![][travis img]][travis]

Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Checkstyle Plugin|2.0|2.1.1|2.2|2.3|2.4|....|3.1
-----------------|---|---|---|---|---|---|---
Checkstyle|5.6|5.6|6.1|6.4.1|6.12.1|...|7.1

A majority of the Checkstyle rules have been introduced in the Sonar Java plugin. Copied rules are marked "Deprecated" in the Checkstyle plugin ONLY beacause Sonar plugin have similar rule but NOT a functionality, [some summary of map/replaced rules (beware of a lot of match mistakes)](http://dist.sonarsource.com/reports/coverage/checkstyle.html) is available.

## Usage
In the quality profile, activate some rules from Checkstyle and run an analysis on your project.

[travis]:https://travis-ci.org/checkstyle/sonar-checkstyle/builds
[travis img]:https://secure.travis-ci.org/checkstyle/sonar-checkstyle.png
