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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        Map<String, String> groupedData = body.stream()
                .collect(Collectors.groupingBy(
                        GenerateRequestModel::getTableName,
                        Collectors.mapping(GenerateRequestModel::getColumnName, Collectors.joining(" "))
                )
        );

        Map<String, List<GenerateRequestModel>> group = body.stream()
                .collect(Collectors.groupingBy(GenerateRequestModel::getTableName));

        group = group.entrySet().stream()
                .map(entry -> {
                    List<GenerateRequestModel> models = entry.getValue();
                    String columns = models.stream().map(GenerateRequestModel::getColumnName).collect(Collectors.joining(" "));
                    String query = String.format("{ %s }", columns);

                    Map<String, String> data = 
                })

         String query = group.entrySet().stream()
                    .map(entry -> {
                        List<GenerateRequestModel> models = entry.getValue();
                        return models.stream()
                                .map(model -> {
                                    String option = model.getOption();
                                    String field = model.getField();
                                    String value = model.getValue();
                                    String columnName = model.getColumnName();
                                    switch(option){

                                        //(where: {id: {_eq: 1}})
                                        case "where": return String.format("%s( where: { %s: {_eq: %s}}){ %s }", entry.getKey(), columnName, field, );

                                        //(order_by: {id: asc})
                                        case "order_by": return String.format("%s ( order_by: { %s: %s}){ %s }", entry.getKey(),field, value, entry.getValue());

                                        //(limit: 2)
                                        case "limit": return String.format("%s ( limit: %s}){ %s }", entry.getKey(),value, entry.getValue());

                                        default:
                                    }
                                })




                        return String.format("%s { %s }", entry.getKey(), entry.getValue());
                    })
                    .collect(Collectors.joining(" "));

            query = String.format("{ \"query\":  \"{ %s }\" }", query);
//            query = String.format("{ \"query\": (%s : \"{\" %s : %s \"}\") \"{ %s }\" }", clause[0], clause[1], clause[2], query);
//        }

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
