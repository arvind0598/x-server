package com.blkx.server.controllers;

import com.blkx.server.beans.DataSourceRegistry;
import com.blkx.server.config.DatasourceConfig;
import com.blkx.server.constants.ResponseMessage;
import com.blkx.server.models.Database;
import com.blkx.server.models.GenerateRequestModel;
import com.blkx.server.models.ResponseModel;
import com.blkx.server.models.TableMetaData;
import com.blkx.server.services.ConfigService;
import com.blkx.server.services.DatabaseService;
import com.blkx.server.services.HasuraService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
public class MainController {

    private DatabaseService databaseService;
    private HasuraService hasuraService;
    private ConfigService configService;

    @Autowired
    private DatasourceConfig datasourceConfig;
    @Autowired
    private DataSourceRegistry registry;

    @Autowired
    public MainController(DatabaseService databaseService, HasuraService hasuraService, ConfigService configService) {
        this.databaseService = databaseService;
        this.hasuraService = hasuraService;
        this.configService = configService;
    }

//    @GetMapping("/hello")
//    public ResponseModel checkController(@RequestParam("name") String name) {
//        String helloName = "hello " + name;
//        return new ResponseModel(false, ResponseMessage.FETCH_SUCCESS.toString(), helloName);
//    }

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
    public ResponseModel getTableNames(@PathVariable("database") String database) throws SQLException {
        ResponseModel response = new ResponseModel();
        System.out.println(registry.getAllSources());
        try{
            databaseService.setActiveDataSource(database);
            try {
                    createHasuraInstance(databaseService.getDbRegistry().get(database));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    @PostMapping("/addsource")
    public ResponseModel addDatabase(@RequestBody Database db) {
        ResponseModel response = new ResponseModel();

        try {
            datasourceConfig.createDataSourceDynamically(db);
            response.setSuccess(true);
            response.setMessage("Database Added");
        }catch (Exception e)
        {
            response.setMessage(ResponseMessage.RANDOM_ERROR.toString());
            response.setSuccess(false);
        }
        return response;
    }

    private void createHasuraInstance(String url) throws InterruptedException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        Info info = dockerClient.infoCmd().exec();
        System.out.print(info +"\n");
        List<String> env = new ArrayList<>();
        env.add("HASURA_GRAPHQL_DATABASE_URL="+url);
        env.add("HASURA_GRAPHQL_ENABLE_CONSOLE=true");
//        env.add("--net=host");
        String id;
        List<Container> containers = dockerClient.listContainersCmd().exec();
        for(Container c: containers) {
            id = c.toString();
            if(id.contains("blk-x-hasura")) {
                try {
                    System.out.println("\n\n\nGot it\n\n" + c.getId());
                    dockerClient.stopContainerCmd(c.getId()).exec().wait();
                    System.out.println("stopped");
                }catch (Exception e) {
                    System.out.println("Stopped");
                }
                try{
                    dockerClient.removeContainerCmd(c.getId()).exec().wait();
                }
                catch(Exception e) {
                    System.out.println("Removed");
                }
            }
        }

        CreateContainerResponse container = dockerClient.createContainerCmd("hasura/graphql-engine:v1.1.0")
                .withName("blk-x-hasura")
                .withHostConfig(HostConfig.newHostConfig().withNetworkMode("host"))
                .withEnv(env)
//                .withPortBindings(PortBinding.parse("8080:8080"))
                .exec();
        System.out.println("AAh");
//        dockerClient.removeContainerCmd(container.getId()).wait();
        try {
            dockerClient.startContainerCmd(container.getId()).exec().wait();
        } catch (Exception e) {
            System.out.println("Started!");
        }
//        dockerClient.waitContainerCmd(container.getId()).exec();
        System.out.println("here");
    }
}
