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
				dir('model-generator') {
					sh 'mvn clean test'
				}
				deployToMaven('jenkins-rarible-ci')
			}
		}
	}
}