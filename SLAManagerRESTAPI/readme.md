![TOREADOR logo](http://www.toreador-project.eu/wp-content/themes/acqualiofilizzata/images/logo-header.png)

The __TOREADOR REST API__ is a collection of REST services that allows the execution of operations against the TOREADOR database. The database is used to store all the meta-data for validation and monitoring projects.
## Project Requirements
1. [Apache Maven](https://maven.apache.org/) v3.5.0 to build and run the project
2. [GIT](https://git-scm.com/) to clone the project for github
3. [MySQL 5.5+](https://www.mysql.com/) database server
## Installation
#### Set up the MySQL database
1. Install the MySQL server
2. Create a user with __{userame}__ and __{password}__
3. Create an empty schema with name __toreador__. Make sure you use this very name for the schema. Also verify that the user can connect remotely to the MySQL server. It might be necessary to grant the user the appropriate rights to allow remote connections
#### Set up the project
4. clone the project locally
```sh
$ git clone https://github.com/kmantzoukas/TOREADOR.git
```
5. Open file __TOREADOR/ToreadorRESTAPI/src/main/resources/db.properties__ and edit the properties according to the MySQL installation performed in the previous step
6. Execute the following commands:
```sh
$ cd TOREADOR/ToreadorRESTAPI
$ mvn clean install tomcat7:run
```
The commands above will instruct maven to clean any previous builds, populate the empty schema created at step 3 with the necessary tables and fire up an instance of [Tomcat 7](tomcat.apache.org/) to deploy and run the project on.

# Profiles
For conveniency and ease of deployment, during development two separate Maven profiles under the names **soi-vm-test01@city** and **localhost** have been created.
**soi-vm-test01@city** is used for the deployment on a Wildfly 8.2 installation at City's cluster and **localhost** is used for the deployment on a local installation for test, debug and development.

  - Use the **soi-vm-test01@city** profile to deploy the services remotely
```sh
$ cd TOREADOR/ToreadorRESTAPI
$ mvn -P soi-vm-test01@city wildfly:deploy
```

  - Use the **localhost** profile to deploy the services locally
```sh
$ cd TOREADOR/ToreadorRESTAPI
$ mvn -P localhost wildfly:deploy
```
All the connection details, necessary for the deployment of the API such as IP address, username, password, etc. are included in the [pom.xml](https://github.com/kmantzoukas/TOREADOR/tree/master/ToreadorRESTAPI/pom.xml) file of the project in the profiles section. 

__IMPORTANT NOTE__: In order to perform any of the operations mentioned above, one needs to be connected at City's VPN at all times. In any other case all the IP addresses mentioned will be unreachable and the system will appear offline.
