# Sonar Checkstyle

[![](https://secure.travis-ci.org/checkstyle/sonar-checkstyle.png)](https://travis-ci.org/checkstyle/sonar-checkstyle/builds)
[![](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Checkstyle_SonarCheckstyleIdeaInspectionsMaster)/statusIcon)](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:Checkstyle_SonarCheckstyleIdeaInspectionsMaster)/statusIcon)
[![](https://sonarcloud.io/api/project_badges/measure?project=checkstyle_sonar-checkstyle&metric=alert_status)](https://sonarcloud.io/dashboard?id=checkstyle_sonar-checkstyle)
[![](https://snyk.io/test/github/checkstyle/sonar-checkstyle/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/checkstyle/sonar-checkstyle?targetFile=pom.xml)

This plugin provides coding rules from [Checkstyle](http://checkstyle.sourceforge.net/) for [Sonar](https://www.sonarsource.com).

---

## Compatiblity

Compatibility matrix from Sonar: [https://docs.sonarsource.com/sonarqube/latest/instance-administration/plugin-version-matrix/](https://docs.sonarsource.com/sonarqube/latest/instance-administration/plugin-version-matrix/)

Compatibility matrix from Checkstyle:

| Checkstyle Plugin | Sonar min | Sonar max | Checkstyle | Jdk |
|-------------------|-----------|-----------|------------|-----|
| 10.21.4           | 9.9       | 10.7+     | 10.21.4    | 11  |
| 10.21.1           | 9.9       | 10.7+     | 10.21.1    | 11  |
| 10.20.2           | 9.9       | 10.7+     | 10.20.2    | 11  |
| 10.20.1           | 9.9       | 10.7+     | 10.20.1    | 11  |
| 10.19.0           | 9.9       | 10.7+     | 10.19.0    | 11  |
| 10.17.0           | 9.9       | 10.0+     | 10.17.0    | 11  |
| 10.16.0           | 9.9       | 10.0+     | 10.16.0    | 11  |
| 10.15.0           | 9.9       | 10.0+     | 10.15.0    | 11  |
| 10.14.2           | 9.9       | 10.0+     | 10.14.2    | 11  |
| 10.14.1           | 9.9       | 10.0+     | 10.14.1    | 11  |
| 10.12.5           | 9.0       | 10.0+     | 10.12.5    | 11  |
| 10.12.3           | 9.0       | 10.0+     | 10.12.3    | 11  |
| 10.12.1           | 9.0       | 10.0+     | 10.12.1    | 11  |
| 10.12.0           | 9.0       | 10.0+     | 10.12.0    | 11  |
| 10.11.0           | 9.0       | 10.0+     | 10.11.0    | 11  |
| 10.9.3            | 9.0       | 10.0+     | 10.9.3     | 11  |
| 10.8.1            | 9.0       | 9.0+      | 10.8.1     | 11  |
| 10.8.0            | 9.0       | 9.0+      | 10.8.0     | 11  |
| 10.7.0            | 9.0       | 9.0+      | 10.7.0     | 11  |
| 10.6.0            | 9.0       | 9.0+      | 10.6.0     | 11  |
| 10.5              | 9.0       | 9.0+      | 10.5       | 11  |
| 10.4              | 9.0       | 9.0+      | 10.4       | 11  |
| 10.3.4            | 9.0       | 9.0+      | 10.3.4     | 11  |
| 10.3.3            | 9.0       | 9.0+      | 10.3.3     | 11  |
| 10.3.2            | 9.0       | 9.0+      | 10.3.2     | 11  |
| 10.3.1            | 9.0       | 9.0+      | 10.3.1     | 11  |
| 10.3              | 9.0       | 9.0+      | 10.3       | 11  |
| 10.2              | 9.0       | 9.0+      | 10.2       | 11  |
| 10.1              | 9.0       | 9.0+      | 10.1       | 11  |
| 10.0              | 9.0       | 9.0+      | 10.0       | 11  |
| 9.3               | 8.9       | 8.9+      | 9.3        | 1.8 |
| 9.2.1             | 8.9       | 8.9+      | 9.2.1      | 1.8 |
| 9.2               | 8.9       | 8.9+      | 9.2        | 1.8 |
| 9.1               | 8.9       | 8.9+      | 9.1        | 1.8 |
| 9.0.1             | 8.9       | 8.9+      | 9.0.1      | 1.8 |
| 8.45.1            | 7.9       | 7.9+      | 8.45.1     | 1.8 |
| 8.42              | 7.9       | 7.9+      | 8.42       | 1.8 |
| 8.41.1            | 7.9       | 7.9+      | 8.41.1     | 1.8 |
| 8.40              | 7.9       | 7.9+      | 8.40       | 1.8 |
| 8.39              | 7.9       | 7.9+      | 8.39       | 1.8 |
| 8.38              | 7.9       | 7.9+      | 8.38       | 1.8 |
| 8.37              | 7.9       | 7.9+      | 8.37       | 1.8 |
| 8.35              | 7.9       | 7.9+      | 8.35       | 1.8 |
| 4.34              | 7.9       | 7.9+      | 8.34       | 1.8 |
| 4.33              | 7.9       | 7.9+      | 8.33       | 1.8 |
| 4.32              | 7.9       | 7.9+      | 8.32       | 1.8 |
| 4.31              | 7.9       | 7.9+      | 8.31       | 1.8 |
| 4.30              | 7.9       | 7.9+      | 8.30       | 1.8 |
| 4.29              | 7.9       | 7.9+      | 8.29       | 1.8 |
| 4.28              | 7.9       | 7.9+      | 8.28       | 11  |
| 4.27              | 6.7       | 7.7+      | 8.27       | 1.8 |
| 4.26              | 6.7       | 7.7+      | 8.26       | 1.8 |
| 4.25              | 6.7       | 7.7+      | 8.25       | 1.8 |
| 4.24              | 6.7       | 7.7+      | 8.24       | 1.8 |
| 4.23              | 6.7       | 7.7+      | 8.23       | 1.8 |
| 4.22              | 6.7       | 7.7+      | 8.22       | 1.8 |
| 4.21              | 6.7       | 7.7+      | 8.21       | 1.8 |
| 4.20              | 6.7       | 7.7+      | 8.20       | 1.8 |
| 4.19              | 6.7       | 7.7+      | 8.19       | 1.8 |
| 4.18              | 6.7       | 7.7+      | 8.18       | 1.8 |
| 4.17              | 6.7       | 7.5       | 8.17       | 1.8 |
| 4.16              | 5.6.6     | 7.2       | 8.16       | 1.8 |
| 4.15              | 5.6.6     | 7.2       | 8.15       | 1.8 |
| 4.14              | 5.6.6     | 7.2       | 8.14       | 1.8 |
| 4.13              | 5.6.6     | 7.2       | 8.13       | 1.8 |
| 4.12              | 5.6.6     | 7.2       | 8.12       | 1.8 |
| 4.11              | 5.6.6     | 7.2       | 8.11       | 1.8 |
| 4.10.1            | 5.6.6     | 7.2       | 8.10.1     | 1.8 |
| 4.10              | 5.6.6     | 7.2       | 8.10       | 1.8 |
| 4.9               | 5.6.6     | 7.2       | 8.9        | 1.8 |
| 4.8               | 5.6.6     | 7.2       | 8.8        | 1.8 |
| 4.7               | 5.6.6     | 7.2       | 8.7        | 1.8 |
| 4.6               | 5.6.6     | 7.2       | 8.6        | 1.8 |
| 4.5               | 5.6.6     | 7.2       | 8.5        | 1.8 |
| 4.4               | 5.6.6     | 7.2       | 8.4        | 1.8 |
| 4.3               | 5.6.6     | 7.2       | 8.3        | 1.8 |
| 4.2               | 5.6.6     | 7.2       | 8.2        | 1.8 |
| 4.1               | 5.6.6     | 7.2       | 8.1        | 1.8 |
| 4.0               | 5.6.6     | 7.2       | 8.0        | 1.8 |
| 3.8               | 5.6.6     | 7.2       | 7.8.2      | 1.8 |
| 3.7               | 5.6.6     | 7.2       | 7.7        | 1.8 |
| 3.6.1             | 5.6.6     | 7.2       | 7.6.1      | 1.8 |
| 3.6               | 5.6.4     | 7.2       | 7.6        | 1.8 |
| 3.5.1             | 5.6.4     | 7.2       | 7.5.1      | 1.8 |
| 3.5               | 5.6.4     | 7.2       | 7.5        | 1.8 |
| 3.4               | 5.6.4     | 7.2       | 7.4        | 1.8 |
| 3.3               | 5.6.4     | 7.2       | 7.3        | 1.8 |
| 3.2               | 5.6.4     | 7.2       | 7.2        | 1.8 |
| 3.1.2             | 5.6.4     | 7.2       | 7.1.2      | 1.8 |
| 3.1.1             | 5.6.4     | 7.2       | 7.1.1      | 1.8 |
| 3.1               | 5.6.4     | --        | 7.1        | 1.8 |
| 2.4               | 4.5.2     | --        | 6.12.1     | 1.7 |
| 2.3               | 4.5.1     | --        | 6.4.1      | 1.7 |
| 2.2               | 4.5.1     | --        | 6.1        | 1.6 |
| 2.1.1             | 3.6       | --        | 5.6        | 1.6 |
| 2                 | 3.6       | --        | 5.6        | 1.6 |

---

## Installation

1. Follow the official [Sonar documentation](https://docs.sonarsource.com/sonarqube/9.8/setup-and-upgrade/install-a-plugin/) to install a plugin from the marketplace, or manually (pick your release from [https://github.com/checkstyle/sonar-checkstyle/releases](https://github.com/checkstyle/sonar-checkstyle/releases).
2. Configure instances from template rules for the `Checkstyle` repository.
3. Add rules/instances from the `Checkstyle` repository to your Quality Profile.
