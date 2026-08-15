pipeline {
    agent any

    environment {
        APP_NAME = "scrap-smart"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${APP_NAME}:${BUILD_NUMBER} ."
                sh "docker tag ${APP_NAME}:${BUILD_NUMBER} ${APP_NAME}:latest"
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    docker stop ${APP_NAME} || true
                    docker rm ${APP_NAME} || true
                    docker run -d \
                        --name ${APP_NAME} \
                        --restart=unless-stopped \
                        -p 8080:8080 \
                        ${APP_NAME}:latest
                """
            }
        }
    }

    post {
        always {
            sh "docker image prune -f"
        }
    }
}
