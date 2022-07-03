# Kotlin  Plugin

Improves pitest's support for Kotlin.

When the plugin is enabled pitest will avoid creating junk mutations in code that uses

* de structuring code
* null casts
* safe casts
* elvis operator

## Usage

The plugin requires pitest 1.7.4. Other versions are not tested.

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
            <version>0.1-SNAPSHOT</version>
          </dependency>
        </dependencies>

        <configuration>
blah
        </configuration>
      </plugin>
   </pluginsugin>
```

or for gradle

```
buildscript {
   repositories {
       mavenCentral()
   }
   configurations.maybeCreate("pitest")
   dependencies {
       classpath 'info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4'
       pitest 'name.stepin:pitest-kotlin-plugin:0.1-SNAPSHOT'
   }
}

apply plugin: "info.solidsoft.pitest"

pitest {
    pitestVersion = "1.7.4"
    targetClasses = ['our.base.package.*']  // by default "${project.group}.*"
}
```
See [gradle-pitest-plugin documentation](http://gradle-pitest-plugin.solidsoft.info/) for more configuration options.

## About


