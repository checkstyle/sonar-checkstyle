Sonar Checkstyle
==========

Official announcement of project transfer - https://groups.google.com/d/topic/sonarqube/HXXxOWS_sOs/discussion

## Description / Features

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/).

Checkstyle Plugin|2.0|2.1.1|2.2|2.3|2.4
-----------------|---|---|---|---|---
Checkstyle|5.6|5.6|6.1|6.4.1|6.12.1

A majority of the Checkstyle rules have been rewritten in the Java plugin. Rewritten rules are marked "Deprecated" in the Checkstyle plugin, but a [concise summary of replaced rules](http://dist.sonarsource.com/reports/coverage/checkstyle.html) is available.

## Usage
In the quality profile, activate some rules from Checkstyle and run an analysis on your project.

## Continuous Integration
[![Build Status](https://travis-ci.org/SonarQubeCommunity/sonar-checkstyle.svg?branch=master)](https://travis-ci.org/SonarQubeCommunity/sonar-checkstyle)
