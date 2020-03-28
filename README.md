# blk-x-server
Server and some database configuration for Project X.

## Problem Statement

Build a portal to be able to create APIs on the fly. We should be able to specify (using drag/drop) the input and specify the output (response) of the API (using drag/drop).

Pre-requisite: Data dictionary of all fields.

We can use graphql and protos for this problem

## What is this repository?

This project is the Spring Boot enabled Java server that runs on port 8081. It currently offers the following routes to use:

1. ```GET /sources``` to fetch a list of all the datasources that are currently configured.
1. ```GET /{database}/tables``` to set the active database and fetch all the tables in it.
1. ```GET /tables/{tableName}```  to fetch a list of all columns that are in a particular table, from the active database.
1. ```POST /generate``` with a list of entities required to generate an API and fetch the UUID at which it is hosted. 
```[{ "tableName": "emp", "columnName": "name", "option": "where", "hasParent": true,	"hasChildren": false,	"value": "val", "field": "2" }]```  
NOTE: option, value, field are optional
1. ```GET /api/{uuid}``` to hit a generated API by passing the UUID that was returned.
1. ```GET /{database}/relations``` to set active database and get relations in given database    
                 Request-> ```"databaseName"```   
                 Response -> ```{"success":true,"message":"Successfully fetched.","data":[{"name":"employee"}]}```

## Setup Instructions

This particular project uses Java 11. Just clone this repository and maven clean install.

However, you would also need to host two containers on a docker network - One for a postgres database and another for Hasura. You would also need to configure these two to have the database preconfigured to a mocked state, and Hasura connected to the PSQL instance.

This should be easy once a docker-compose.yaml file is made, but until then use the following commands to set up the instances, assuming of course that Docker is installed and properly configured (you might need to sudo some of these commands):

First, ```docker ps --all``` and make sure that no instances of blk-x-postgres or blk-x-hasura are currently saved. Delete them if they exist.

### Postgres

 ```shell script
docker run --rm -d --name blk-x-postgres \
  -e POSTGRES_PASSWORD=blkpassword \
  -p 5432:5432 \
  postgres
```

Then ```docker ps``` to verify that Postgres is running on port 5432.
Connect to the database using:

```shell script
docker exec -it blk-x-postgres bash
```

Within the container, connect to the PSQL database, and then create a database using:
```shell script
psql --u postgres
```

```postgresql
create database blk_x;
\c blk_x;
```

Now make tables.

### Hasura

```shell script
docker run --rm -d --name blk-x-hasura \
  -e HASURA_GRAPHQL_DATABASE_URL=postgres://postgres:blkpassword@localhost:5432/blk_x \
  -e HASURA_GRAPHQL_ENABLE_CONSOLE=true \
  --net=host \
  hasura/graphql-engine:v1.1.0
```

Then ```docker ps``` to verify that Postgres is running on port 8080.
