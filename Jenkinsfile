@Library('shared-library') _

pipeline {
	agent any

	options {
		disableConcurrentBuilds()
	}

	stages {
		stage('deploy') {
			when {
				branch 'master'
			}
			steps {
				sh 'mvn clean'
				dir('model-generator') {
					sh 'mvn test'
				}
				deployToMaven('jenkins-rarible-ci')
			}
		}
	}
}