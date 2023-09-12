# License Plugin with Git for Gradle
A Gradle plugin that provides tasks that format the copyright using a template and verify if the copyright is
correctly applied. In the template, you can specify pre-defined properties for years to be replaced with the
creation year, last modified year or the combination of them using Git history.
This plugin is based on the original [license-maven-plugin-git](https://github.com/mathieucarbou/license-maven-plugin/tree/master/license-maven-plugin-git) for Maven.

:warning: This is not intended to and do not constitute legal advice of any circumstances.

## Usage
The plugin provides the following tasks:
- `licenseFormat`: Updates the copyright in your project files using a template.
- `license`: Verifies if the copyright is correctly updated.

To run the tasks, include this plugin in your build script:
```groovy
plugins {
    id 'com.linecorp.gradle.license-git' version '0.1.0'
}
```
Create a file named `LICENSE` under the `{Project Root}/settings/license_template` directory of your project and place the copyright to apply:
```
Copyright 2023 LY Corporation
Licensed under the Apache License ...
```
- You can put the file in a different place and specify the path using the `license.header` property
  in your `build.gradle`.
  ```groovy
  license {
      header = file('path/to/LICENSE')
  }
  ```
Now, you can run the task via the command line:
```shell
$ ./gradlew licenseFormat
```

## Configuration
The plugin can be configured using the license closure in your `build.gradle` file.
Here are available configuration options:
- `header`: the file that has the copyright template.
- `headerURI`: the URI of the file that has the header template.
- `inceptionYear`: the inception year of the project.
- `includePatterns`: the file patterns to include for processing. Defaults to `**/*`.
- `excludePatterns`: the file patterns to exclude from processing. Defaults to an empty set.
- `ignoreFailures`: prevent tasks from stopping the build, defaults to `false`.
- `dryRun`: show what would happen if the task was run, defaults to `false`.
- `skipExistingHeaders`: whether to skip files that already have a header, which might not be the same
  as the one in the template. Defaults to `false`.
- `useDefaultMappings`: use a long list of standard mapping, defaults to `true`. See [Supported comment types](http://code.mycila.com/license-maven-plugin/#supported-comment-types) for the complete list.
- `mapping`: add a mapping between a file extension and a style type.
- `strictCheck`: be extra strict in the formatting of existing headers, defaults to `false`.

## Copyright year properties
You can use the following properties surrounded by `${}` in the template to specify the year.
- `license.git.copyrightLastYear`: the year of the last change of the present file as seen in git history
- `license.git.copyrightYears`: the combination of `project.inceptionYear` and `license.git.copyrightLastYear`
  delimited by a dash (`-`), or just `project.inceptionYear` if `project.inceptionYear` is equal to
  `license.git.copyrightLastYear`
- `license.git.copyrightCreationYear`: the year of the first commit of the present file as seen in git history
- `license.git.copyrightExistenceYears`: similar to `license.git.copyrightYears` but using
  `license.git.copyrightCreationYear` for the first year

For example, if you use the following template:
```
Copyright ${license.git.copyrightExistenceYears} LY Corporation
```
and the file was created in 2020 and last modified in 2021, the file's copyright will be:
```
/*
 * Copyright 2020-2021 LY Corporation
 */
```
