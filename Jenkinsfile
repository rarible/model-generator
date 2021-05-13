@Library('shared-library') _

pipeline {
	agent any

	options {
		disableConcurrentBuilds()
	}

	stages {
		stage('test') {
			steps {
				dir('model-generator') {
					sh 'mvn clean test'
				}
			}
		}
		stage('deploy') {
			when {
				branch 'master'
			}
			steps {
				deployToMaven('jenkins-rarible-ci')
			}
		}
	}
}