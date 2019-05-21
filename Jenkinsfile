pipeline {
  agent none
  environment {
	  PROJECT="pipelinetest"
	  APPNAME="witcom-api-gateway"
  }  
  stages {
      stage('Init'){
	  agent { label 'master' }
          steps {
            echo 'Hello on ${BRANCH_NAME}...'
            sh 'printenv'
          }
          
      }
      stage('Build & Test'){
         when {
             expression { env.CHANGE_ID != null }
         } 
		 agent { label 'master' }
         steps {
            sh 'echo Building ${BRANCH_NAME}...'
            sh 'mvn clean compile -Pdev -DskipTests=true'
            sh 'mvn test'
         }
      }
      stage('Deployment to Master') {
          when {
              branch 'master'
          }
		  stages {
			stage('Build JAR'){
				agent { label 'maven' }
					steps {
						sh "mvn clean package -Pprod -DskipTests=true"
						sh "mv target/witcom-api-gateway-1.0.0.jar target/app.jar"
						stash name:"jar", includes:"target/app.jar"
					}
			}
			stage('Building image in target project'){
				agent { label 'master' }
				steps {
				unstash name:"jar"
				script {
					configFileProvider([configFile(fileId: '59897b24-bba7-42d4-8edc-98995d9f7b81', variable: 'buildPropertiesFile')]) {
						def jsonfile = readJSON file: "${buildPropertiesFile}"
						def targetProject = jsonfile.devProject
						echo "Target-Project for Master: ${targetProject}"
					}
					timeout(time: 20, unit: 'MINUTES') {
						openshift.withCluster() {
						  echo "Target-Project for Master 2: ${targetProject}"
						  openshift.withProject(targetProject) {
						    def bc = openshift.selector('bc', [deployment: 'dev', app: 'witcom-api-gateway'])
							def buildSelector = bc.startBuild("--from-file=target/app.jar")
							echo "Found ${bc.count()} buildconfig - expecting 1"
							def blds = bc.related('builds')
							blds.untilEach {
							  return it.object().status.phase == "Complete"
							}
						  }
						}  
					}
				}
			}
			}
		  }		  

      }
  }
}


def isPr() {
    env.CHANGE_ID != null
}

