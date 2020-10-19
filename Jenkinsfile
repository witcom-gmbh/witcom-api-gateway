def gatewayVersion = 'UNKNOWN'
def dockerRepo = 'pub-registry.dev.witcom.services/witcom/witcom-api-gateway'
def gitRepo = 'https://github.com/witcom-gmbh/witcom-api-gateway.git'
def approvalRequired = false;
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
					    //sh 'mvn test'
				    }
			    } // end stage build&test
			    stage('Verify'){
                    when {
                        expression { approvalRequired }
                    }                    
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
			    } // end stage-verify
		    } //end stages for pull-request
        }
        stage('Create and publish docker-image from master') {
            when {
                branch 'master'
            }
            stages {
                stage('Init build'){
                    agent { label 'master' }
                    steps {
	                    script  {
	                        gatewayVersion = readMavenPom().getVersion();
	                    }	        
	                    stash name:"openshiftbc",includes:"*.yaml"
                    }
                }
                stage('Build'){
                    agent { label 'maven' }
                    steps {
	                    sh "mvn clean package -Pprod -DskipTests=true"
	                    sh "mv target/witcom-api-gateway-${gatewayVersion}.jar target/app.jar"
	                    stash name:"jar", includes:"target/app.jar"
                    }
                }
                stage('Create OpenShift buildconfigs') {
                    agent { label 'master' }
                    steps {
                        unstash name:"openshiftbc"
                        script {
                            //delete existing buildconfig
                            openshift.withCluster() {
                                openshift.withProject() {
                                    def existingBC = openshift.selector('bc', [app: 'witcom-api-gateway'])
                                    if(existingBC){
                                        existingBC.delete();
                                    }
                                }
                            }
                            def bc = readYaml file: './openshift-bc-runtime.yaml'
                            bc.spec.output.to.name = "${dockerRepo}:${gatewayVersion}"
                            bc.metadata.name = "witcom-api-gateway-runtime-${gatewayVersion}"
                            bc.metadata.labels["version"]=gatewayVersion
                            timeout(time: 1, unit: 'MINUTES') {
                            openshift.withCluster() {
                                openshift.withProject() {
                                def fromYaml = openshift.create( bc )
                                echo "Created Buildconfig: ${fromYaml.names()}"
                                }
                            }  
                            }
                            //create bc for latest-tag 
                            bc.spec.output.to.name = "${dockerRepo}:latest"
                            bc.metadata.name = "witcom-api-gateway-runtime-latest"
                            bc.metadata.labels["version"]="latest"
                            timeout(time: 1, unit: 'MINUTES') {
                            openshift.withCluster() {
                                openshift.withProject() {
                                def fromYaml = openshift.create( bc )
                                echo "Created Buildconfig: ${fromYaml.names()}"
                                }
                            }  
                            }
                        } //end script
                    } //end steps
                } //end stage
                stage('run version docker build and push ') {
                    agent { label 'master' }
                    steps {
                        script {
		                    timeout(time: 20, unit: 'MINUTES') {
                            openshift.withCluster() {
                                openshift.withProject() {
                                def bc = openshift.selector('bc', [app: 'witcom-api-gateway',version:gatewayVersion])
                                def buildSelector = bc.startBuild("--from-file=target/app.jar")
                                echo "Found ${bc.count()} buildconfigs - expecting 1"
                                def blds = bc.related('builds')
                                blds.untilEach() {
                                    return it.object().status.phase == "Complete"
                                }
                                //delete build configs
                                echo "Deleting the buildconfig....."
                                bc.delete()

                                }
                            }  
                        } // end timeout                
                        } // end script
                    } //end steps
                } // end stage     
                stage('run latest docker build and push') {
                    agent { label 'master' }
                    steps {
                        script {
                        timeout(time: 20, unit: 'MINUTES') {
                            openshift.withCluster() {
                                openshift.withProject() {
                                def bc = openshift.selector('bc', [app: 'witcom-api-gateway',version:'latest'])
                                def buildSelector = bc.startBuild("--from-file=target/app.jar")
                                echo "Found ${bc.count()} buildconfigs - expecting 1"
                                def blds = bc.related('builds')
                                blds.untilEach() {
                                    return it.object().status.phase == "Complete"
                                }
                                //delete build configs
                                echo "Deleting the buildconfig....."
                                bc.delete()

                                }
                            }  
                        } // end timeout                
                        } // end script
                    } //end steps
                } // end stage
            }     
        }// End Create and publish docker-image from master
    } // end stages
}
