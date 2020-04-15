#!/bin/bash
/usr/bin/docker stop blk-x-hasura
/usr/bin/docker run --rm -d --name blk-x-hasura -e HASURA_GRAPHQL_DATABASE_URL=$1 -e HASURA_GRAPHQL_ENABLE_CONSOLE=true --net=host hasura/graphql-engine:v1.1.0
