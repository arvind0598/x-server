package com.blkx.server;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);

		String schema = "type Query{hello: String}";
		SchemaParser schemaParser = new SchemaParser();
		TypeDefinitionRegistry registry = schemaParser.parse(schema);

		RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
				.type("Query", builder -> builder.dataFetcher("hello", new StaticDataFetcher("world")))
				.build();

		SchemaGenerator schemaGenerator = new SchemaGenerator();
		GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(registry, wiring);

		GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
		ExecutionResult result = build.execute("{hello}");
		System.out.println(result.getData().toString());
	}

}
