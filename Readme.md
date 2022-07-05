# Kotlin  Plugin

Improves pitest's support for Kotlin.

When the plugin is enabled pitest will avoid creating junk mutations in code that uses

* de structuring code
* null casts
* safe casts
* elvis operator

This project is based on github.com/pitest/pitest-kotlin but it's rewritten to be able to maintain it.

There is sample project: http://github.com/stepin/pitest-kotlin-sample

Feel free to post bug reports and PRs.

## Usage

The plugin requires pitest 1.7.4 and Kotlin 1.7.0. Other versions are not tested.

To activate the plugin it must be placed on the classpath of the pitest tool (**not** on the classpath of the project being mutated).

e.g for maven

```xml
    <plugins>
      <plugin>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-maven</artifactId>
        <version>1.7.4</version>
        <dependencies>
          <dependency>
            <groupId>name.stepin</groupId>
            <artifactId>pitest-kotlin-plugin</artifactId>
            <version>0.3.0</version>
          </dependency>
        </dependencies>

        <configuration>
blah
        </configuration>
      </plugin>
   </pluginsugin>
```

for gradle

```groovy
buildscript {
   repositories {
       mavenCentral()
   }
   configurations.maybeCreate("pitest")
   dependencies {
       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4'
       pitest 'name.stepin:pitest-kotlin-plugin:0.3.0'
   }
}

apply plugin: "info.solidsoft.pitest"

pitest {
    pitestVersion = "1.7.4"
    targetClasses = ['our.base.package.*']  // by default "${project.group}.*"
}
```

or for Gradle Kotlin DSL:

```kotlin
plugins {
    id("info.solidsoft.pitest") version "1.7.4"
...
}
repositories {
    mavenCentral()
...
}
dependencies {
    pitest("name.stepin:pitest-kotlin-plugin:0.1.0")
    pitest("org.pitest:pitest-junit5-plugin:0.15")
...
}
```

See [gradle-pitest-plugin documentation](http://gradle-pitest-plugin.solidsoft.info/) for more configuration options.

## Support policy

I'm using this project on daily basis. So, it should work.

If you have any ideas / bugs please submit GitHub Issue. More details -- faster to resolve.
It's always a good idea to show use-case in sample project (http://github.com/stepin/pitest-kotlin-sample).

## Todo

There are plans to release version 1.0.0 somewhere in 07.22 after following tasks:

- Uncomment detekt
- Uncomment 2 tests
- Refactor tests to better show use-cases
- Prepare sample project
- Upgrade to pitest version 1.9.0
