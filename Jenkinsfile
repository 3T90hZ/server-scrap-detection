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
                sh '''
                    cp /opt/scrap-smart/.env .
                    docker compose down --remove-orphans || true
                    docker rm -f scrap-smart mysql || true
                    docker compose up -d --build
                '''
            }
        }
    }

    post {
        always {
            sh "docker image prune -f"
        }
        success {
            echo "Deployment successful! App is running on port 8081"
        }
        failure {
            echo "Deployment failed. Please check the logs."
        }
    }
}
