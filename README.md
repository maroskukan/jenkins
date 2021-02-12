# Jenkins

- [Jenkins](#jenkins)
  - [Introduction](#introduction)
  - [Documentation](#documentation)
  - [Installation](#installation)
  - [](#)

## Introduction

Jenkins is an open source automation server. It can help automate building, testing and deploying software. 


## Documentation

- [Jenkins Project](https://www.jenkins.io/)


## Installation

There are two main releases available. The Long-Term Support (LTS) and Regular releases (Weekly). As name implies, the first one is recommended from production whereas the second one have latest available features.

Once you selected the release, you have number of ways when it comes to deployment options. Few options include:
- Generic Java package (.war)
- Docker
- Ubuntu/Debian
- CentOS/Fedora/Red Hat
- Windows
- and more...

Using [Docker image](https://hub.docker.com/r/jenkins/jenkins) is interesting since the installation is as easy as running the following command:
```bash
docker run -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```

Once the container is initialize, proceed to `http://localhost:8080` to perform initial setup. The Administrator password credential should be available in log messages. If you missed it for any reason, you can retrieve it via docker exec, where `98ed` is container id:
```bash
docker exec -it 98ed cat /var/jenkins_home/secrets/initialAdminPassword
446dd585cefe4a658381847ccc8232c0
```


## 




