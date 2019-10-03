package de.datlag.hotdrop.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class InteraAccount {

    private String response;
    private JsonObject object;
    private String username;
    private boolean error = true;
    private String email;
    private String description;
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

    public String getResponse() {
        return this.response;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean getError() {
        return this.error;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean getVerified() {
        return this.verified;
    }

}
