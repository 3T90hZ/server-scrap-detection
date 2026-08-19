pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

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
                    docker compose config --quiet
                    docker compose up -d --remove-orphans
                    docker compose ps
                '''
            }
        }
    }

    post {
        always {
            sh "rm -f .env"
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
