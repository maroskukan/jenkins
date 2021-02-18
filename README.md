# Jenkins

- [Jenkins](#jenkins)
  - [Introduction](#introduction)
  - [Documentation](#documentation)
  - [Installation](#installation)
  - [Restart](#restart)
  - [Docker compose](#docker-compose)
  - [Initial Setup](#initial-setup)
    - [Plugin installation](#plugin-installation)
    - [Admin user](#admin-user)
    - [Instance Configuration](#instance-configuration)
  - [Managing Jenkins](#managing-jenkins)
    - [Web UI Layout](#web-ui-layout)
    - [User settings](#user-settings)
  - [Freestyle Jobs](#freestyle-jobs)
    - [Manual Build](#manual-build)
    - [Jenkins Build](#jenkins-build)
      - [Capturing artifacts](#capturing-artifacts)
      - [Test result report](#test-result-report)
  - [Build Triggers](#build-triggers)
  - [Pipeline Project](#pipeline-project)
  - [Converting Freestyle to Pipeline](#converting-freestyle-to-pipeline)
  - [Colocating Jobs and Source Code](#colocating-jobs-and-source-code)
    - [Build Triggers](#build-triggers-1)
    - [Build Notifications](#build-notifications)
    - [Jenkinsfile and SCM](#jenkinsfile-and-scm)
    - [Dynamic Pipeline discovery](#dynamic-pipeline-discovery)
  - [Tips](#tips)
    - [Inspecing Volume](#inspecing-volume)
    - [Volume Permissions](#volume-permissions)
    - [Logging into container](#logging-into-container)
    - [Running Petclinic from container](#running-petclinic-from-container)
    - [Ace Editor Shortcuts](#ace-editor-shortcuts)

## Introduction

Jenkins is an open source automation server. It can help automate building, testing and deploying software. 


## Documentation

- [Jenkins Project](https://www.jenkins.io/)
- [Plugins Index](https://plugins.jenkins.io/)
- [Pipeline Examples](http://jenkins.io/doc/pipeline/examples)

## Installation

There are two main releases available. The Long-Term Support (LTS) and Regular releases (Weekly). As name implies, the first one is recommended from production whereas the second one have latest available features.

Once you selected the release, you have number of ways when it comes to deployment options. Few options include:
- Generic Java package (.war)
- Docker
- Ubuntu/Debian
- CentOS/Fedora/Red Hat
- Windows
- and many more...

Using [Docker image](https://hub.docker.com/r/jenkins/jenkins) is interesting since the installation is as easy as running the following command:
```bash
docker run -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```

Once the container is initialize, proceed to `http://localhost:8080` to perform initial setup. The Administrator password credential should be available in log messages. If you missed it for any reason, you can retrieve it via docker exec, where `98ed` is container id:
```bash
docker exec -it 98ed cat /var/jenkins_home/secrets/initialAdminPassword
446dd585cefe4a658381847ccc8232c0
```

## Restart

Once you perfrom the initial configuration described below, the settings are stored persistenly in docker volume. Therefore even if you restart the container this data ramains intact. This is especially useful during upgrades and experimenting with new version.

## Docker compose

You can use Docker compose files to create a small testing environment running on your machine. An example `docker-compose.yml` is available at `docker/compose` directory.

```bash
cd docker/compose
docker-compose up
Creating volume "compose_jenkins_home" with default driver
Creating compose_mails_1   ... done
Creating compose_jenkins_1 ... done
Attaching to compose_mails_1, compose_jenkins_1
mails_1    | 2021/02/12 12:16:22 Using in-memory storage
mails_1    | 2021/02/12 12:16:22 [SMTP] Binding to address: 0.0.0.0:1025
mails_1    | 2021/02/12 12:16:22 Serving under http://0.0.0.0:8025/
mails_1    | [HTTP] Binding to address: 0.0.0.0:8025
```

```bash
docker-compose ps
      Name                     Command               State                  Ports
------------------------------------------------------------------------------------------------
compose_jenkins_1   /sbin/tini -- /usr/local/b ...   Up      50000/tcp, 127.0.0.1:8080->8080/tcp
compose_mails_1     MailHog                          Up      1025/tcp, 127.0.0.1:8025->8025/tcp
```

Optinally, create a host name record of `jenkins` and `mail` mapped to `127.0.0.1`. For example:
```ini
# To map jenkins to localhost
127.0.0.1 jenkins
127.0.0.1 mails
```

To cleanup use `docker-compose down`
```
docker-compose down
Stopping compose_jenkins_1 ... done
Stopping compose_mails_1   ... done
Removing compose_jenkins_1 ... done
```


## Initial Setup

### Plugin installation

Once logged in with initial password, you are presented with two options:
- Install suggested plugins (Install plugins the Jenkins community finds most useful)
- Select plugins to install (Select and install plugins most suitable for your needs)

The full list of available plugins is located at [Plugins Index](https://plugins.jenkins.io/).

When, select `Select plugins to install`. You will be presented with a list of plugins divided into different categories such as:
- Organizaiton and Administration
- Build Features
- Build Tools
- Build Analysis and Reporting
- many more...

There will be several plugins already selected, you can read about them at Plugins index for more information.

For now, select the `Install suggested plugins` to get started quickly. You can alwasy add more later using `Plugins Manager`.

### Admin user

Once the plugins are sucessfully installed, you are required to configure the first Admin user. Fill out the required fields and hit `Save and Continue` to proceed to Instance Configuration.

### Instance Configuration

This is the place where you can adjust Jenkins URL, which is set to `http://localhost:8080/` by default. 

From the description:
```
The Jenkins URL is used to provide the root URL for absolute links to various Jenkins resources. That means this value is required for proper operation of many Jenkins features including email notifications, PR status updates, and the BUILD_URL environment variable provided to build steps.
The proposed default value shown is not saved yet and is generated from the current request, if possible. The best practice is to set this value to the URL that users are expected to use. This will avoid confusion when sharing or viewing links.
```

If you are not sure, you can skip this step for now and adjust it at `Manage Jenkins` page in web UI.

Complete the setup wizard by hitting the `Start using Jenkins`.

## Managing Jenkins

### Web UI Layout
The main system configuration parameters are located at `Manage Jenkins` tab. Here you can find:
- System Configuration
- Security
- Status Information
- Troubleshooting
- Tools and Actions

### User settings
You should start by creating a new user. Navigate to `Manage Jenkins > Security > Manage Users`. Hit `Create User`.By default, this new user can do anything, as you can see in `Manage Jenkins > Security > Configure Global Security > Authorization `.

Plugins may augment the security settings, for example integrating with enterprise directory solutions.


## Freestyle Jobs

First, lets review the usual process of application build cycle which may be composed of several steps:
1. Clean the artifacts of previous build
2. Locating the application code
3. Downloading the application code to workspace folder
4. Installing any build dependencies
5. Building (compiling) the application
6. Running automated tests
7. Packaging the application

### Manual Build
Before you add build automation to your project, make sure the build itself works on your machine. Therefore in case of an issue, you are not dealing with two areas at the same time. 

Start by getting a sample application code for Spring Petclinic.

```bash
git clone https://github.com/spring-projects/spring-petclinic.git
cd spring-petclinic
```

**Note: You need to have Java installed before you can proceed further. For example:**
```bash
# Install Openjdk
sudo apt install openjdk-11-jdk
# Verify Java Version
java --version
openjdk 11.0.10 2021-01-19
OpenJDK Runtime Environment (build 11.0.10+9-Ubuntu-0ubuntu1.20.04)
OpenJDK 64-Bit Server VM (build 11.0.10+9-Ubuntu-0ubuntu1.20.04, mixed mode, sharing)
```

Then using [Maven](https://maven.apache.org/), `compile` option, compile the source code. 
```bash
./mvnw compile
Downloading https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.3.3/apache-maven-3.3.3-bin.zip
........................................................................................................................................................................................................................................................................................................................................................................................................................
Unzipping /home/maros/.m2/wrapper/dists/apache-maven-3.3.3-bin/3opbjp6rgl6qp7k2a6tljcpvgp/apache-maven-3.3.3-bin.zip to /home/maros/.m2/wrapper/dists/apache-maven-3.3.3-bin/3opbjp6rgl6qp7k2a6tljcpvgp
Set executable permissions for: /home/maros/.m2/wrapper/dists/apache-maven-3.3.3-bin/3opbjp6rgl6qp7k2a6tljcpvgp/apache-maven-3.3.3/bin/mvn
[INFO] Scanning for projects...
Downloading: https://repo.maven.apache.org/maven2/io/spring/platform/platform-bom/2.0.6.RELEASE/platform-bom-2.0.6.RELEASE.pom
Downloaded: https://repo.maven.apache.org/maven2/io/spring/platform/platform-bom/2.0.6.RELEASE/platform-bom-2.0.6.RELEASE.pom (40 KB at 57.6 KB/sec)
...
[ Output omitted ]
...
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.690 s
[INFO] Finished at: 2021-02-15T19:02:36+01:00
[INFO] Final Memory: 25M/110M
[INFO] ------------------------------------------------------------------------
```

Once the build is successful, a new `target` folder will be presented. To move to next phase, invoke `test` option.
```bash
./mvnw test
[INFO] Scanning for projects...
[INFO]
[INFO] ------------< org.springframework.samples:spring-petclinic >------------
[INFO] Building petclinic 2.4.2
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from spring-snapshots: https://repo.spring.io/snapshot/org/apache/maven/plugins/maven-surefire-plugin/2.22.2/maven-surefire-plugin-2.22.2.pom
...
[ Output omitted ]
...
[INFO]
[INFO] Results:
[INFO]
[WARNING] Tests run: 40, Failures: 0, Errors: 0, Skipped: 1
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  44.944 s
[INFO] Finished at: 2021-02-15T19:25:32+01:00
[INFO] ------------------------------------------------------------------------
```

Finally, package the application with `package` option.
```bash
./mvnw package
[INFO] Scanning for projects...
[INFO]
[INFO] ------------< org.springframework.samples:spring-petclinic >------------
[INFO] Building petclinic 2.4.2
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from spring-snapshots: https://repo.spring.io/snapshot/org/apache/maven/plugins/maven-jar-plugin/3.2.0/maven-jar-plugin-3.2.0.pom
Downloading from spring-milestones: https://repo.spring.io/milestone/org/apache/maven/plugins/maven-jar-plugin/3.2.0/maven-jar-plugin-3.2.0.pom
...
[ Output omitted ]
...
[INFO] Building jar: /home/maros/code/maroskukan/spring-petclinic/target/spring-petclinic-2.4.2.jar
[INFO]
[INFO] --- spring-boot-maven-plugin:2.4.2:repackage (repackage) @ spring-petclinic ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  42.810 s
[INFO] Finished at: 2021-02-15T19:26:46+01:00
[INFO] ------------------------------------------------------------------------
```

When all steps are successfull, you can run the application.
```bash
java -jar -Dserver.port=8081 target/spring-petclinic-2.4.2.jar


              |\      _,,,--,,_
             /,`.-'`'   ._  \-;;,_
  _______ __|,4-  ) )_   .;.(__`'-'__     ___ __    _ ___ _______
 |       | '---''(_/._)-'(_\_)   |   |   |   |  |  | |   |       |
 |    _  |    ___|_     _|       |   |   |   |   |_| |   |       | __ _ _
 |   |_| |   |___  |   | |       |   |   |   |       |   |       | \ \ \ \
 |    ___|    ___| |   | |      _|   |___|   |  _    |   |      _|  \ \ \ \
 |   |   |   |___  |   | |     |_|       |   | | |   |   |     |_    ) ) ) )
 |___|   |_______| |___| |_______|_______|___|_|  |__|___|_______|  / / / /
 ==================================================================/_/_/_/

:: Built with Spring Boot :: 2.4.2


2021-02-15 19:28:12.343  INFO 28297 --- [           main] o.s.s.petclinic.PetClinicApplication     : Starting PetClinicApplication v2.4.2 using Java 11.0.10 on slayer with PID 28297 (/home/maros/code/maroskukan/spring-petclinic/target/spring-petclinic-2.4.2.jar started by maros in /home/maros/code/maroskukan/spring-petclinic)
```

Petclic is now available at `http://localhost:8081/`. 

### Jenkins Build

Start by creating a new `Job` or `Project` and select `Freestyle project`. Fill out the following fields in `General` tab:

| Field          | Value                                                   |
| -------------- | ------------------------------------------------------- |
| Name           | Petclinic                                               |
| Description    | Gotta Love Them                                         |
| SCM            | Git                                                     |
| Repository URL | https://github.com/spring-projects/spring-petclinic.git |

At this stage, it is good to verify if Jenkins can access the required repository. You can do that from project overview, using `Build Now`. The first time you run the build it may fail due to default branch name change, such as `main` instead of `master`. This can be adjusted at job configuration.

Once the job is defined a new `Workspace` folder is created inside `jenkins_home`. This is the place where all required files for project are stored.

Next we need to define the `Build` process. We can add first build step using `Execute shell` option.
```bash
./mvnw compile
```

Once the first build is sucessfull, you can change the the build details, and package the application.
```bash
./mvnw package
```

Once the application is packaged, the output should be available in the workspace directory. You verify this from project overview.

#### Capturing artifacts

When you run a job multiple times, the chance is that produced files will be overwritten by latest build. Therefore if you need to keep tham you need to configure `Post-build Actions`, within project configuration.

An example of such Action could be `Archive the artifacts`. With the `Files to archive` set to `target/*.jar`. 

Once you save the updated configuration, and run another build, the latest generated artifact will be available from main Project page, as well as on Build page.

#### Test result report

For Java based projects, it may be interesting to also capture the `JUnit test result report`. This is available in `Post-build Actions`. The `Test report XMLs` path is `target/surefire-reports/*.xml`

Once you rerun the job, `Test Result` is available on the job summary.


## Build Triggers

So far, we have been triggering build manually from Jenkins console. In production, you will likely want to implement an automatic build trigger. 

Based on approach you can select from these default options:
- Trigger builds remotely (e.g. from scripts)
- Build after other projects are built
- Build periodically
- GitHub hook trigger for GITScm pooling
- Poll SCM

You can also expand the options by installing appropriate plugins.

For now select `Poll SCM` and configure a schedule. Once it is configured, a new menu item `Git Polling Log` will appear with content like this:

```bash
Started on Feb 16, 2021 3:04:00 PM
Using strategy: Default
[poll] Last Built Revision: Revision e2fbc561309d03d92a0958f3cf59219b1fc0d985 (refs/remotes/origin/main)
The recommended git tool is: NONE
No credentials specified
 > git --version # timeout=10
 > git --version # 'git version 2.11.0'
 > git ls-remote -h -- https://github.com/spring-projects/spring-petclinic.git # timeout=10
Found 3 remote heads on https://github.com/spring-projects/spring-petclinic.git
[poll] Latest remote head revision on refs/heads/main is: e2fbc561309d03d92a0958f3cf59219b1fc0d985 - already built by 8
Done. Took 1 sec
No changes
```

Now, when you make a change to the source repository, Jenkins should pick that up and trigger a build.


## Pipeline Project

Freestyle job may not be well suited for more complex build processes that require conditions, and complex operations. A more flexible option is available using Pipelines.

Create a new item using `Pipeline` option. Name the item as `spc`. You will be presented with the following tabs:
- General
- Build Triggers
- Advanced Project Options
- Pipeline

Pipiline tab is particularly internesting, it provides sample scripts that define the build process. For example `Github + Maven`.

```groovy
pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "M3"
    }

    stages {
        stage('Build') {
            steps {
                // Get some code from a GitHub repository
                git 'https://github.com/jglick/simple-maven-project-with-tests.git'

                // Run Maven on a Unix agent.
                sh "mvn -Dmaven.test.failure.ignore=true clean package"

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }

            post {
                // If Maven was able to run the tests, even if some of the test
                // failed, record the test results and archive the jar file.
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}
```

This definition will be encoded into Project configuration `/var/jenkins_home/jobs/spc/config.xml`as follows:

```xml
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <keepDependencies>false</keepDependencies>
  <properties/>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>jenkins@b60687a29ed0:~/jobs/spc$ cat config.xml
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description></description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition" plugin="workflow-cps@2.89">
    <script>pipeline {
    agent any

    tools {
        // Install the Maven version configured as &quot;M3&quot; and add it to the path.
        maven &quot;M3&quot;
    }

    stages {
        stage(&apos;Build&apos;) {
            steps {
                // Get some code from a GitHub repository
                git &apos;https://github.com/jglick/simple-maven-project-with-tests.git&apos;

                // Run Maven on a Unix agent.
                sh &quot;mvn -Dmaven.test.failure.ignore=true clean package&quot;

                // To run Maven on a Windows agent, use
                // bat &quot;mvn -Dmaven.test.failure.ignore=true clean package&quot;
            }

            post {
                // If Maven was able to run the tests, even if some of the test
                // failed, record the test results and archive the jar file.
                success {
                    junit &apos;**/target/surefire-reports/TEST-*.xml&apos;
                    archiveArtifacts &apos;target/*.jar&apos;
                }
            }
        }
    }
}
</script>
    <sandbox>true</sandbox>
  </definition>
  <triggers/>
  <disabled>false</disabled>
```

The easiest way to start creating a custom script is to take an example defined above and modify it according the current needs. Using `Snippet Generator` inside the `Pipeline Syntax` help is very useful in these situations.

The following is a base skeleton for our project:
```groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Get some code from a GitHub repository
                git url: 'https://github.com/maroskukan/spring-petclinic.git', branch: 'main' 
            }
        }
        stage('Build') {
            steps {
                // Run Maven Wrapper
                sh './mvnw clean package'
            }
          
            post {
              always {
                  junit '**/target/surefire-reports/TEST-*.xml'
                  archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}
```

More Pipeline examples can be found at [Jenkins Docs](http://jenkins.io/doc/pipeline/examples)

## Converting Freestyle to Pipeline

There is a `Convert To Pipeline` plugin available that can convert a Freestyle project to Pipeline project. Once installed, navigate to a Freestyle project main page, and click `Convert to Pipeline`.

## Colocating Jobs and Source Code

It is possible to colocate pipeline script `ide/pipeline.groovy` outside of Jenins and inside a version control system or your local development machine . 

One of the methods to develop locally is to use `Jenkins Runner` vscode extension which allows you to execute local pipeline script in remote Jenkins instance.

Example configuration can be found in `.vscode/settings.json` file. To invoke this script, open command pallet in VScode `CTRL` + `Shift` + `P` and run `Jenkins Runner: Run Pipeline Script On Default Job`.

### Build Triggers

Build Triggers allow you to define when to run a job. There are number of options and parameters available:
- Build after another projects are built
- Build periodically
- Github hook trigger for GITscm polling
- Poll SCM
- Disable this project
- Quiet period
- Trigger builds remotely (e.g. from scripts)

This is similar to Freestyle project configuration, however since we are dealing with pipeline script, we can define these parameters inside it with the help of Declarative Generator.

### Build Notifications

In order to leverage email notification for intersting events (such as build failure) you first need to configure the `Extended E-mail Notification` in Jenkins `System Configuration`.

| Name        |   Value |
| ----------- | ------- |
| SMTP Server | mails   |
| SMTP Port   | 1025    |

Also update the `Jenkins Location` if required.

| Name        |   Value |
| ----------- | ------- |
| Jenkins URL | http://jenkins:7080/ |

### Jenkinsfile and SCM

Another option is to fetch Pipeline configuration (Jenkinsfile) from source control manager, for example from Github.

For this reason there is a new `Jenkisfile` in the test repository `maroskukan/spring-petclinic`.

In Jenkins UI, create a new item called `spc-jenkinsfile` with Pipeline type. Define the following pipeline script configuration.

| Name        |   Value |
| ----------- | ------- |
| Definition | Pipeline script from SCM |
| SCM | Git |
| Repositories | https://github.com/maroskukan/spring-petclinic |
| Branch Specifier | */main |
| Script Path | Jenkinsfile |

Save and initiate the first build. The Jenkins file will be automatically loaded from repository and defined actions will be executed.

### Dynamic Pipeline discovery

So far we always had to define and configure new items in Jenkins UI before we could initiate the build process. 

If you have dynamic environment, where new projects are created and deleted on regular basis, it is recommended to add Github organization to jenkins, instead of individual repositories.

This is accomplished by creating and configuring new item with type of `GitHub Organization`. The following minimum configuration is required.

| Name        |   Value |
| ----------- | ------- |
| Name | org-mkukan |
| API Endpoint | https://api.github.com |
| Owner | maroskukan |
| Behaviors | Discovered branches |
| Pipeline Jenkinsfile | Jenkinsfile |

Once you click `Save` Jenkins file scan the Organization/Account in Github, and create Job for any discovered repositories that contain Jenkinsfile.

If you want to a self service capability, you could create a **Jenkins maintenance** repository where you would store the organization configuraiton and create a jenkins job to update itself when a change happens in the organization list file.


## Tips

### Inspecing Volume

If you need to browse the created docker volume used for persistent file storage.
```bash
docker volume ls
docker run -it --rm -v jenkins_home:/vol busybox ls -l /vol
total 104
-rw-r--r--    1 1000     1000          1647 Feb 12 11:26 config.xml
-rw-r--r--    1 1000     1000           100 Feb 12 11:25 copy_reference_file.log
-rw-r--r--    1 1000     1000           156 Feb 12 11:26 hudson.model.UpdateCenter.xml
-rw-r--r--    1 1000     1000          1243 Feb 12 11:14 hudson.plugins.emailext.ExtendedEmailPublisher.xml
-rw-r--r--    1 1000     1000           370 Feb 12 10:55 hudson.plugins.git.GitTool.xml
-rw-------    1 1000     1000          1712 Feb 12 10:31 identity.key.enc
[ Output omitted ]
```

### Volume Permissions

If you are experiencing permission errors with mapped folders such as:
```bash
jenkins_1  | touch: cannot touch '/var/jenkins_home/copy_reference_file.log': Permission denied
jenkins_1  | Can not write to /var/jenkins_home/copy_reference_file.log. Wrong volume permissions?
compose_jenkins_1 exited with code 0
```

You may need to change the folder owner ship on host machine
```bash
sudo chown -v -R 1000:1000 jenkins_home_on_host
changed ownership of 'jenkins_home_on_host' from root:root to 1000:1000
```

### Logging into container

If you need to inspect filesystem directly from running container you can use `docker exec`.

```bash
docker-compose exec jenkins bash
```

### Running Petclinic from container

When you generated an artifact during build you can quickly test it using `openjdk` container.
```bash
docker run --rm -p 8081:8081 -v "$PWD":/usr/src/myapp -w /usr/src/myapp openjdk java -jar -Dserver.port=8081 ./spring-petclinic-2.4.2.jar
```

### Ace Editor Shortcuts

The embedded editor for Pipeline script uses ace editor. Some useful shortcuts can be found [here](https://ace.c9.io/demo/keyboard_shortcuts.html)





























