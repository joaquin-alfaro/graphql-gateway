# graphql-gateway
API gateway to expose rest services using GraphQL specification.

## Overview
Image of architecture

Modules
### graphql-server

### graphql-schema

### graphql-registry

### graphql-registry-client

## How it works
State diagram

## Setup
Configuration of springboot service to register in graphql-gateway

#### Dependency with *graphql-registry-client*
```xml
<dependency>
    <groupId>org.formentor</groupId>
    <artifactId>graphql-registry-client</artifactId>
    <version>${graphql-gateway.version}</version>
</dependency>   
```
#### Annotation of spring boot class with *@GraphQLRegistryService*
```java
@SpringBootApplication
@GraphQLRegistryService
public class BooksApplication {
	public static void main(String[] args) {
		SpringApplication.run(BooksApplication.class, args);
	}
}
```
#### Configuration
Configuration of the url of the *graphql-gateway* in *application.yaml*
```yaml
graphql:
  registry:
    uri: http://localhost:8080
```
## Usage
1. Install
```shell
mvn clean package
```
1. Start *graphql-gateway*
```shell
java -jar graphql-gateway-server/target/graphql-gateway.jar
```
2. Start *rest-books-service*
```shell
java -jar rest-books-service/target/*.jar
```
3. Start *rest_countries_service*
```shell
java -jar rest-countries-service/target/*.jar
```
4. Execute GraphQL queries using GraphQL Playground

## Caveats
- It should be a cluster to support high availability
- It can be deployed in kubernetes?
- Replace the module *graphql-registry* with an existing *api gateway* or *service mesh* 
- Allow the registration of GraphQL APIs