pipeline {
  agent {
    label 'maven'
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B clean compile'
      }
    }
    stage('Test') {
      steps {
        sh 'mvn -B test'
      }
    }
    stage('Deploy') {
      when {
        anyOf {
          branch 'develop'
          branch 'master'
        }
      }
      steps {
        sh 'mvn -B -P deploy deploy'
      }
    }
    stage('Snapshot Site') {
      when {
        branch 'develop'
      }
      steps {
        sh 'mvn -B site-deploy'
      }
    }
    stage('Release Site') {
      when {
        branch 'master'
      }
      steps {
        sh 'mvn -B -P gh-pages-site site site:stage scm-publish:publish-scm'
      }
    }
  }
}