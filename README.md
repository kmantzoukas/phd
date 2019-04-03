# Install Java 7+
Install the latest version of Java. You can follow the installation instructions [here](https://java.com/en/download/manual.jsp)

# Install MySQL 5.6+
Install any version of the community MySQL later than version 5.6. After the installation of the community server, create a new empty schema under the name __scdf__

# Install Maven 3.5+
Install the latest version of Maven. You can follow the installation instructions [here](https://maven.apache.org/install.html)

# Start Spring Cloud Dataflow server V2.0.1
To start Spring Cloud Dataflow execute the following command:

```sh
$ java -jar spring-cloud-dataflow-server-2.0.1.RELEASE.jar \
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