# Jenkins

- [Jenkins](#jenkins)
  - [Introduction](#introduction)
  - [Documentation](#documentation)
  - [Installation](#installation)
  - [Initial Setup](#initial-setup)
    - [Plugin installation](#plugin-installation)
    - [Admin user](#admin-user)
    - [Instance Configuration](#instance-configuration)
  - [Managing Jenkins](#managing-jenkins)
    - [Web UI Layout](#web-ui-layout)
    - [User settings](#user-settings)

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


































