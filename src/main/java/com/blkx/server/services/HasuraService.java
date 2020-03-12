package com.blkx.server.services;

import com.blkx.server.beans.DataSourceRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class HasuraService {

    private final String HASURA_URL = "http://localhost:8080";
    private final DataSourceRegistry registry;
    private final HttpClient client;

    @Autowired
    public HasuraService(DataSourceRegistry registry) {
        this.registry = registry;
        this.client = HttpClient.newHttpClient();
    }

    public Map<String, Object> fetchData(String query) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HASURA_URL + "/v1/graphql"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();
        HttpResponse<String> queryResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(queryResponse.body(), new TypeReference<>() {});
    }

    private JsonNode processRelations(ArrayNode tables) {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        ArrayNode data = factory.arrayNode();

        for(JsonNode node: tables) {
            String tableName = node.get("table").get("name").asText();
            ObjectNode tableData = factory.objectNode();
            tableData.put("name", tableName);

            ArrayNode arrayRelations = factory.arrayNode();
            ArrayNode arrayRelationsNode = (ArrayNode) node.get("array_relationships");
            if(arrayRelationsNode != null) {
                for(JsonNode relation: arrayRelationsNode) {
                    String relationName = relation.get("name").asText();
                    JsonNode keyNode = relation.get("using").get("foreign_key_constraint_on");
                    String destTable = keyNode.get("table").get("name").asText();
                    String destColumn = keyNode.get("column").asText();

                    ObjectNode arrayRelation = factory.objectNode();
                    arrayRelation.put("relationName", relationName);
                    arrayRelation.put("destTable", destTable);
                    arrayRelation.put("destColumn", destColumn);
                    arrayRelations.add(arrayRelation);
                }
                tableData.set("arrayRelations", arrayRelations);
            }

            ArrayNode objectRelations = factory.arrayNode();
            ArrayNode objectRelationsNode = (ArrayNode) node.get("object_relationships");
            if(objectRelationsNode != null) {
                for(JsonNode relation: objectRelationsNode) {
                    String relationName = relation.get("name").asText();
                    String sourceColumn = relation.get("using").get("foreign_key_constraint_on").asText();

                    ObjectNode objectRelation = factory.objectNode();
                    objectRelation.put("relationName", relationName);
                    objectRelation.put("sourceColumn", sourceColumn);
                    objectRelations.add(objectRelation);
                }
                tableData.set("objectRelations", objectRelations);
            }

            data.add(tableData);
        }
        return data;
    }

    public JsonNode fetchRelationships() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HASURA_URL + "/v1/query"))
                .headers("Content-Type", "application/json")
                .headers("X-Hasura-Role", "admin")
                .POST(HttpRequest.BodyPublishers.ofString("{\"type\":\"export_metadata\",\"args\":{}}"))
                .build();
        HttpResponse<String> queryResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(queryResponse.body());

        ArrayNode tablesNode = (ArrayNode) rootNode.get("tables");
        System.out.println(tablesNode);

        return processRelations(tablesNode);
    }
}
