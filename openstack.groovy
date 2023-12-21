pipeline {
    agent any

    environment {
        OS_AUTH_URL = 'http://10.0.2.15/identity'
        OS_USERNAME = 'admin'
        OS_PASSWORD = 'terralogic'
        OS_PROJECT_NAME = 'admin'
        OS_PROJECT_DOMAIN_NAME = 'default'
        OS_USER_DOMAIN_NAME = 'default'
        OS_REGION_NAME = 'RegionOne'
        OS_IMAGE_NAME = 'cirros-0.6.2-x86_64-disk'
        OS_FLAVOR_NAME = 'm1.small'
        OS_NETWORK_NAME = 'private_network'
        OS_INSTANCE_NAME = 'new-instance'
        OS_SECURITY_GROUP = 'private_security'
        OS_KEY_PAIR_NAME = 'private_keypair'
        OS_PUBLIC_KEY_PATH = '/home/ramesh/.ssh/id_rsa.pub'
    }

    stages {
        stage('Authenticate with OpenStack') {
            steps {
                script {
                    // Authenticate with OpenStack
                    sh """
                        openstack --os-username $OS_USERNAME \
                                  --os-password $OS_PASSWORD \
                                  --os-project-name $OS_PROJECT_NAME \
                                  --os-auth-url $OS_AUTH_URL \
                                  --os-user-domain-name $OS_USER_DOMAIN_NAME \
                                  --os-project-domain-name $OS_PROJECT_DOMAIN_NAME \
                                  --os-region-name $OS_REGION_NAME \
                                  token issue -f value -c id > /tmp/openstack_token
                    """

                    // Get the OpenStack authentication token
                    def token = readFile('/tmp/openstack_token').trim()
                    env.OPENSTACK_TOKEN = token
                }
            }
        }

        stage('Create Security Group') {
            steps {
                script {
                    // Your code to create a security group
                    sh "openstack security group create $OS_SECURITY_GROUP"
                }
            }
        }

        stage('Create Key Pair') {
            steps {
                script {
                    // Your code to create a key pair
                    sh "openstack keypair create --public-key $OS_PUBLIC_KEY_PATH $OS_KEY_PAIR_NAME"
                }
            }
        }

        stage('Create OpenStack Instance') {
            steps {
                script {
                    // Create an instance in OpenStack
                    sh """
                        openstack --os-token $OPENSTACK_TOKEN \
                                  --os-url $OS_AUTH_URL \
                                  server create --flavor $OS_FLAVOR_NAME \
                                  --image $OS_IMAGE_NAME \
                                  --network $OS_NETWORK_NAME \
                                  --key-name $OS_KEY_PAIR_NAME \
                                  $OS_INSTANCE_NAME
                    """
                }
            }
        }
    }
}