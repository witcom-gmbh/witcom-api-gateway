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
         agent { label 'master' } 
		 stages {
			stage('Build & test'){
				agent { label 'maven' }
				steps {
				    sh 'mvn clean compile -Pdev -DskipTests=true'
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
			sh "mv target/witcom-api-gateway-*.jar target/app.jar"
			stash name:"jar", includes:"target/app.jar"
			stash name:"openshift",includes:"*.yaml"
	}
      }
      stage('Build from OpenShift - Start building Runtime-Image'){
        when {
          environment name: 'BUILD_FROM', value: 'openshift'
        }
        agent { label 'master' }
        stages {
        	stage('Create & run buildconfig'){
        	 steps {
        	   unstash name:"openshift" 
        	   //todo change buildconfig, e.g. set tag to version  
        	   script {
		    timeout(time: 1, unit: 'MINUTES') {
		      openshift.withCluster() {
		        openshift.withProject() {
		          def fromYaml = openshift.create( readFile( 'openshift-bc-runtime.yaml' ) )
		          echo "Created objects from YAML file: ${fromYaml.names()}"
		        }
		      }  
		    }
		   }
		   unstash name:"jar"
		  script {
		    timeout(time: 20, unit: 'MINUTES') {
		      openshift.withCluster() {
		        openshift.withProject() {
		          def bc = openshift.selector('bc', [app: 'witcom-api-gateway'])
		          def buildSelector = bc.startBuild("--from-file=target/app.jar")
		          echo "Found ${bc.count()} buildconfig - expecting 1"
		          def blds = bc.related('builds')
		          blds.untilEach {
		            return it.object().status.phase == "Complete"
		          }
		          bc.delete()
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

