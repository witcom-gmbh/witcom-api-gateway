pipeline {
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
      stage('Build from OpenShift - Init'){
        when {
          environment name: 'BUILD_FROM', value: 'openshift'
        }
        steps {
        script {
          openshift.setLockName('openshift-deploy-witcom-api-gateway')
        }
        }
      }
      stage('Build from OpenShift - Build JAR'){
        when {
          environment name: 'BUILD_FROM', value: 'openshift'
        }
        agent { label 'maven' }
				steps {
						sh "mvn clean package -Pprod -DskipTests=true"
						sh "mv target/witcom-api-gateway-1.0.0.jar target/app.jar"
						stash name:"jar", includes:"target/app.jar"
				}
      }
      stage('Build from OpenShift - Start building Runtime-Image'){
        when {
          environment name: 'BUILD_FROM', value: 'openshift'
        }
        agent { label 'master' }
        steps {
          unstash name:"jar"
          script {
            timeout(time: 20, unit: 'MINUTES') {
              openshift.withCluster() {
                openshift.withProject() {
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
      stage('Deployment to Master') {
          when {
		  anyOf {
			  branch 'master'
		  }
          }
		  stages {
		    stage('Init'){
				steps {
					script {
						openshift.setLockName('openshift-deploy-witcom-api-gateway')
					}
				}
			}
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
					try {
						configFileProvider([configFile(fileId: '59897b24-bba7-42d4-8edc-98995d9f7b81', variable: 'buildPropertiesFile')]) {
							def jsonfile = readJSON file: "${buildPropertiesFile}"
							env.PROJECT = jsonfile.devProject
						}
					} catch (err){
						echo "in catch block for config-file provider"
						echo "Caught: ${err}"
						currentBuild.result = 'FAILURE'
						throw err
					}
					timeout(time: 20, unit: 'MINUTES') {
						openshift.withCluster() {
						  openshift.withProject(env.PROJECT) {
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

