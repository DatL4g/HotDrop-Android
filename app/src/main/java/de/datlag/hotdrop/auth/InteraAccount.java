package de.datlag.hotdrop.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;

public class InteraAccount {

    @Getter
    private String response;

    private JsonObject object;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private boolean error = true;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private boolean verified = false;

    public InteraAccount(String response) {
        this.response = response;
        init();
    }

    private void init() {
        object = new Gson().fromJson(response, JsonObject.class);
        setError(object.get("error").getAsBoolean());

        if (!error) {
            setUsername(object.get("username").getAsString());
            setEmail(object.get("mail").getAsString());
            setDescription(object.get("description").getAsString());
            setVerified(object.get("verified").getAsBoolean());
        }
    }

}
