package com.blkx.server.constants;

public enum ResponseMessage {
    FETCH_SUCCESS("Successfully fetched."),
    TEMP_MESSAGE("You have successfully hit this endpoint."),
    INVALID_CONFIG("The configuration entered was invalid."),
    VALID_CONFIG("New configuration saved successfully."),
    RANDOM_ERROR("There was an error.");

    String message;

    ResponseMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
