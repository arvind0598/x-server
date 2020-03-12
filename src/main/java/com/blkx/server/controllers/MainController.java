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
    public ResponseModel generateAPI(@RequestBody List<GenerateRequestModel> body, @RequestBody String[] clause, @RequestBody boolean condition) {
        ResponseModel response = new ResponseModel();
        String query;

        Map<String, String> groupedData = body.stream()
                .collect(Collectors.groupingBy(
                        GenerateRequestModel::getTableName,
                        Collectors.mapping(GenerateRequestModel::getColumnName, Collectors.joining(" "))
                )
        );

        if(!condition) {
            query = groupedData.entrySet().stream()
                    .map(entry -> String.format("%s { %s }", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" "));

            query = String.format("{ \"query\":  \"{ %s }\" }", query);
        }
        else {
            query = groupedData.entrySet().stream()
                    .map(entry -> String.format("%s { %s }", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" "));

            query = String.format("{ \"query\": (%s : \"{\" %s : %s \"}\") \"{ %s }\" }", clause[0], clause[1], clause[2], query);
        }

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
