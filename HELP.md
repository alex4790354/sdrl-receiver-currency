# Read Me First

Feed (2d) micro-service for loading data from the RF Central Bank into the RabbitMQ queue


# Getting Started
1) Install Docker
2) Start micro-service 'sdrl-feed-cbr'.
3) Request the data (using Postman collection or requests from sdrl-feed-cbr.HELP.md)
4) Check JSONs in RabbitMQ queues.
5) Add containers from 'docker-compose.yml' (java-postgres-receiver)
6) Start application
7) Check data in DB: jdbc:postgresql://localhost:5430/cbr (usr/pwd), using selects. For example:
   - SELECT * FROM cbr.currency;
   - SELECT * FROM cbr.currency_rate;
   - SELECT * FROM cbr.currency_rate_history;
   - SELECT * FROM cbr.metal;
   - SELECT * FROM cbr.metal_rate
        WHERE code = 1 AND effective_date > TO_DATE('01-01-2023', 'dd-MM-yyyy')
        ORDER BY effective_date;
   - SELECT count(*) from cbr.metal_rate;   


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.2.5/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.5/reference/htmlsingle/index.html#web)
* [MyBatis Framework](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [MyBatis Quick Start](https://github.com/mybatis/spring-boot-starter/wiki/Quick-Start)

