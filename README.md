These instruction assume that one has navigated in the cloned folder and therefore all the paths to files and folders are shown with that in mind.

# Install Java 7+
Install the latest version of Java. You can follow the installation instructions [here](https://java.com/en/download/manual.jsp)

# Install MySQL 5.6+
Install any version of the community MySQL later than version 5.6. After the installation of the community server, create a new empty schema under the name __scdf__

# Install Maven 3.5+
Install the latest version of Maven. You can follow the installation instructions [here](https://maven.apache.org/install.html)

# Build the individual services from source
To build and create the executable Java code for all the services you need to navigate to the individual services in the services folder and run the following commands:

```sh
$ cd services/laodandanonymize/
$ ./mvnw clean pacakge

$ cd services/preparedata/
$ ./mvnw clean pacakge

$ cd services/computeaverages/
$ ./mvnw clean pacakge
```

Within the target folder of each project a jar file will be created with the executable code. Note that you might have to make the mvnw script executable by executing the following commands:

```sh
$ cd services/laodandanonymize/
$ chmod +x mvnw

$ cd services/preparedata/
$ chmod +x mvnw

$ cd services/computeaverages/
$ chmod +x mvnw
```

# Start Spring Cloud Dataflow server V2.0.1
To start Spring Cloud Dataflow execute the following command:

```sh
$ java -jar scdf/spring-cloud-dataflow-server-2.0.1.RELEASE.jar \
--spring.datasource.url=jdbc:mysql://{dbhost}:{dbport}/scdf \
--spring.datasource.username={username} \
--spring.datasource.password={password} \
--spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

Please use the table below to correctly fill in the command above.

| Placeholder | value |
| ------ | ------ |
| `${username}` | Username of the user to connect to the MySQL database |
| `${password}` | Password of the user to connect to the MySQL database |
| `${dbhost}` | Hostname or IP address where the MySQL database is installed |
| `${dbport}` | Port number where the MySQL database is listening for connections |

The MySQL database is used to persist applications, tasks and task executions. That means that all the metadata will be permanently stored even after Spring cloud Datflow has been shutdown. If one wishes to run the server without the ability to store all these metadata, the Spring Cloud Dataflow Server can be started without inluding any of the parameters shown in the table above. In that case the server will use an in-memory ephimeral H2 database that will be destroed as soon as the server is closed. 

To run the server witout the persistanse capabilities of MySQL run the following command:

```sh
$ java -jar scdf/spring-cloud-dataflow-server-2.0.1.RELEASE.jar
```

# Spring Cloud Dataflow shell V2.0.1
To load the relevant applications and tasks with the assistance of Spring Cloud Dataflow Shell execute the following command:

```sh
$ java -jar scdf/spring-cloud-dataflow-shell-2.0.1.RELEASE.jar \
--spring.shell.commandFile=services/scdf-apps-tasks
```

Note that the __Spring Cloud Dataflow Server__ and the __Spring Cloud Dataflow Shell__ must run on the same machine. If this is not the case then the URI of the server must be provided with the ___--dataflow.uri___ parameter when starting the shell.