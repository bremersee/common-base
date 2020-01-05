pipeline {
  agent {
    label 'maven'
  }
  tools {
    jdk 'jdk8'
    maven 'm3'
  }
  stages {
    stage('Tools') {
      steps {
        sh 'java -version'
        sh 'mvn -B --version'
      }
    }
    stage('Test') {
      when {
        not {
          branch 'feature/*'
        }
      }
      steps {
        sh 'mvn -B clean test'
      }
    }
    stage('Deploy Feature') {
      when {
        branch 'feature/*'
      }
      steps {
        sh 'mvn -B -P feature,allow-features clean deploy'
      }
    }
    stage('Deploy') {
      when {
        anyOf {
          branch '1.4.develop'
          branch '1.4.master'
        }
      }
      steps {
        sh 'mvn -B -P deploy clean deploy'
      }
    }
    stage('Site') {
      when {
        anyOf {
          branch '1.4.develop'
          branch '1.4.master'
        }
      }
      steps {
        sh 'mvn -B site-deploy'
      }
    }
  }
}