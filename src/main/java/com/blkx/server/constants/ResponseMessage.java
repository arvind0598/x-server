package com.blkx.server.constants;

public enum ResponseMessage {
    FETCH_SUCCESS("Successfully fetched."),
    TEMP_MESSAGE("You have successfully hit this endpoint."),
    INVALID_CONFIG("The configuration entered was invalid."),
    VALID_CONFIG("New configuration saved successfully."),
    RANDOM_ERROR("There was an error."),
    INVALID_PATH("The API you tried to reach doesn't exist."),
    ADD_SUCCESS("The API has been generated and is now live."),
    SEND_SUCCESS("The datasource list has been sent successfully");

    String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
