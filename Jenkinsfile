pipeline {
    tools {
        maven 'M3'
        jdk 'OPENJDK11'
    }
    environment { 
        JAVA_TOOL_OPTIONS = '-XX:+UnlockExperimentalVMOptions -Dsun.zip.disableMemoryMapping=true'
    }

  agent none
  stages {
      stage('Process Pull-Request'){
         when {
             expression { env.CHANGE_ID != null }
         } 
		 stages {
			stage('Build & test'){
				agent { label 'maven' }
				steps {
				    sh 'mvn clean compile -Pdev -DskipTests=true'
				}
			}
			stage('Verify'){
				steps {
					script {
						def userInput
						try {
							userInput = input(
								id: 'Proceed1', message: 'Kann der Merge durchgefuehrt werden ?', parameters: [
								[$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Bitte bestaetigen']
								])
						} catch(err) { // input false
							userInput = false
							echo "This Job has been Aborted"
						}
						if (userInput != true) {
							throw "Pull-request not confirmed"
						}
					}
				}
			}
		 }
      }
  }
}

