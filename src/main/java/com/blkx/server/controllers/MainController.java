package com.blkx.server.controllers;

import com.blkx.server.constants.ResponseMessage;
import com.blkx.server.models.ResponseModel;
import com.blkx.server.models.TableMetaData;
import com.blkx.server.services.DatabaseService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
public class MainController {

    private DatabaseService databaseService;

    @Autowired
    public MainController(DatabaseService databaseService) {
        this.databaseService = databaseService;
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

    @GetMapping("/tables")
    public ResponseModel getTableNames() {
        ResponseModel response = new ResponseModel();
        try {
            List<String> tableNames = databaseService.getTableNames();
            response.setSuccess(true);
            response.setMessage(ResponseMessage.FETCH_SUCCESS.toString());
            response.setData(tableNames);
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
        } catch (SQLException e) {
            response.setSuccess(false);
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
            e.printStackTrace();
        }
        return response;
    }

}
