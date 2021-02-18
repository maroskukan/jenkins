//Declarative //
pipeline {
    agent any
    triggers {
        pollSCM('* * * * *')
    }
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
                //sh 'false' // true
            }
          
            post {
                always {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
              }
              changed {
                emailext subject: "Job \'${JOB_NAME}\' (${BUILD_NUMBER}) ${currentBuild.result}",
                    body: "Please go to ${BUILD_URL} and verify the build",
                    attachLog: true, 
                    compressLog: true,
                    to: "test@jenkins", 
                    recipientProviders: [upstreamDevelopers(), requestor()]
              }
            }
        }
    }
}