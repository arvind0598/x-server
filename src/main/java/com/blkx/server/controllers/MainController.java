package com.blkx.server.controllers;

import com.blkx.server.constants.ResponseMessage;
import com.blkx.server.models.GenerateRequestModel;
import com.blkx.server.models.ResponseModel;
import com.blkx.server.models.TableMetaData;
import com.blkx.server.services.ConfigService;
import com.blkx.server.services.DatabaseService;
import com.blkx.server.services.HasuraService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
public class MainController {

    private DatabaseService databaseService;
    private HasuraService hasuraService;
    private ConfigService configService;

    @Autowired
    public MainController(DatabaseService databaseService, HasuraService hasuraService, ConfigService configService) {
        this.databaseService = databaseService;
        this.hasuraService = hasuraService;
        this.configService = configService;
    }

    @GetMapping("/hello")
    public ResponseModel checkController(@RequestParam("name") String name) {
        String helloName = "hello " + name;
        return new ResponseModel(false, ResponseMessage.FETCH_SUCCESS.toString(), helloName);
    }

    @PostMapping("/credentials")
    public ResponseModel postCredentials(@RequestBody ObjectNode node) {
        String url = node.get("url").asText();
        String username = node.get("username").asText();
        String password = node.get("password").asText();

        ResponseModel response = new ResponseModel();
        try {
            this.databaseService.postCredentials(url, username, password);
            response.setSuccess(true);
            response.setMessage(ResponseMessage.VALID_CONFIG.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.INVALID_CONFIG.toString());
        }
        return response;
    }

    @GetMapping("/{database}/tables")
    public ResponseModel getTableNames(@PathVariable("database") String database) {
        ResponseModel response = new ResponseModel();
        try {
            databaseService.setActiveDataSource(database);
            List<String> tableNames = databaseService.getTableNames();
            response.setSuccess(true);
            response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
            response.setData(tableNames);
        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.INVALID_DATASOURCE.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
        }
        return response;
    }

    @GetMapping("/tables/{name}")
    public ResponseModel getTableData(@PathVariable("name") String name) {
        ResponseModel response = new ResponseModel();
        try {
            TableMetaData data = databaseService.getTableData(name);
            response.setSuccess(true);
            response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
            response.setData(data);
        } catch (NullPointerException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.INVALID_DATASOURCE.toString());
        } catch (SQLException e) {
            response.setSuccess(false);
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
            e.printStackTrace();
        }
        return response;
    }

    @PostMapping("/generate")
    public ResponseModel generateAPI(@RequestBody List<GenerateRequestModel> body) {
        ResponseModel response = new ResponseModel();
        int index;
        String conditions = "";
        Map<String, String> queryMapCond = new HashMap<>();

        for(GenerateRequestModel g: body){
            if(g.getOption()!= null)
            {
                switch (g.getOption()) {
                    case "where":
                        conditions = String.format("(where: {%s:{ %s: %s}})", g.getColumnName(), g.getField(), g.getValue());
                        break;
                    case "order_by":
                        conditions = String.format("(order_by: {%s: %s})", g.getColumnName(), g.getValue());
                        break;
                }
                queryMapCond.put(g.getTableName(),conditions);
            }
            else {
                conditions = "";
            }

        }

        Map<String, String> innerGroupedData = body.stream()
                .filter(GenerateRequestModel::getHasParent)
                .collect(Collectors.groupingBy(
                        GenerateRequestModel::getTableName,
                        Collectors.mapping(GenerateRequestModel::getColumnName, Collectors.joining(" "))
                )
        );

        Map<String, String> innerQueries = innerGroupedData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.format("%s { %s }", entry.getKey(), entry.getValue())));

        System.out.println(innerQueries);

        Map<String, List<GenerateRequestModel>> partialGroupedData = body.stream()
                .filter(model -> !model.getHasParent())
                .collect(Collectors.groupingBy(
                        GenerateRequestModel::getTableName
                        )
                );

        Map<String, String> groupedData = partialGroupedData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                        List<GenerateRequestModel> models = entry.getValue();
                        return models.stream()
                                .map(model -> {
                                    if (model.getHasChildren()) {
                                        String name = model.getColumnName();
                                        return innerQueries.get(model.getColumnName());
                                    }
                                    else {
                                        return model.getColumnName();
                                    }
                                })
                                .collect(Collectors.joining(" "));
                        }
                    )
                );

        String query = groupedData.entrySet().stream()
                .map(entry -> String.format("%s { %s }", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" "));

//        System.out.println(query);

        for(Map.Entry<String, String> g : queryMapCond.entrySet()){
            if(query.contains(g.getKey()))
            {
                StringBuilder builder = new StringBuilder(query);
                index = query.indexOf(g.getKey());
                builder.insert(index + g.getKey().length(), " " + queryMapCond.get(g.getKey()) + " ");
                query = builder.toString();
            }
        }

        System.out.println(query);
        query = String.format("{ \"query\":  \"{ %s }\" }", query);

        UUID uuid = configService.insertNewQuery(query);
        response.setSuccess(true);
        response.setMessage(ResponseMessage.ADD_SUCCESS.toString());
        response.setData(uuid.toString());
        return response;
    }

    @GetMapping("/api/{uuid}")
    public ResponseModel performQuery(@PathVariable("uuid") String uuidStr) {
        ResponseModel response = new ResponseModel();
        try {
            UUID uuid = UUID.fromString(uuidStr);
            String query = configService.getQuery(uuid);
            if(query == null) throw new IllegalArgumentException();

            Map<String, Object> responseData = hasuraService.fetchData(query);

            response.setSuccess(true);
            response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
            response.setData(responseData);
        }
        catch (IllegalArgumentException e) {
            response.setSuccess(false);
            response.setMessage(ResponseMessage.INVALID_PATH.toString());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
        }
        return response;
    }

    @GetMapping("/sources")
    public ResponseModel getDatasources(){
        ResponseModel response = new ResponseModel();
        List<String> dataSources = databaseService.getDataSources();
        response.setSuccess(true);
        response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
        response.setData(dataSources);
        return response;
    }

    @GetMapping("/{database}/relations")
    public ResponseModel getRelations(@PathVariable("database") String database) {
        ResponseModel response = new ResponseModel();
        try {
            databaseService.setActiveDataSource(database);
            JsonNode responseData = hasuraService.fetchRelationships();
            response.setSuccess(true);
            response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
            response.setData(responseData);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
        }
        return response;
    }
}
