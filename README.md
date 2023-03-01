*Warning!* This repository is no longer maintained.

Sonar File Alerts Plugin
========================

This plugin raises alerts on file level in Sonar. It extends default behaviour, which raises alerts only at root project level. It is useful when you create alert rules like "Code Coverage < 60". Each file is checked against this rule!

It is intended to use in conjunction with our other plugin: [Sonar Gerrit Plugin](https://github.com/TouK/sonar-gerrit-plugin). It builds and report Gerrit's awaiting patchsets.

Requirements
------------

- SonarQube 4.0 (plugin is build against 4.0's API)

*Note: you should be able to build against Sonar's 3.x API with ease, but it won't be supported. Just change version in pom.xml and fix small code issues.*

Installation
------------

There is a build package available here: [sonar-file-alerts-plugin-1.0.jar](https://github.com/TouK/sonar-file-alerts-plugin/releases/download/sonar-file-alerts-plugin-1.0/sonar-file-alerts-plugin-1.0.jar).

Or you can build it for yourself. Clone this repository, package it and put a package to your sonar plugins directory.

```bash
mvn package
cp target/sonar-file-alerts-plugin-1.0.jar $SONAR_DIR/plugins
$SONAR_DIR/bin/your-architecture-here/sonar.sh restart
```

How does it work?
-----------------

Plugin works as a Sonar's decorator. It just check measures against file, not only projects. It is mainly a copy of [CheckAlertThresholds.java](https://github.com/SonarSource/sonar/blob/master/plugins/sonar-core-plugin/src/main/java/org/sonar/plugins/core/sensors/CheckAlertThresholds.java?source=c) from original Sonar code.

License
-------

This project is licenced under Apache License.
