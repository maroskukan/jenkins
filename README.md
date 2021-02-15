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
  - [Tips](#tips)
    - [Inspecing Volume](#inspecing-volume)

## Introduction

Jenkins is an open source automation server. It can help automate building, testing and deploying software. 


## Documentation

- [Jenkins Project](https://www.jenkins.io/)
- [Plugins Index](https://plugins.jenkins.io/)


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

Before you add build automation to your project, make sure the build itself works on your machine. Therefore in case of an issue, you are not dealing with two areas at the same time. 

Start by getting a sample application code.

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


































