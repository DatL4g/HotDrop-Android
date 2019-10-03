package de.datlag.hotdrop.auth;

import org.jetbrains.annotations.Contract;

public enum ProviderEnum {

    GOOGLE("google.com"),
    EMAIL("password"),
    GITHUB("github.com"),
    ANONYM("anonym");

    private String providerId;
    private ProviderEnum(String providerId) {
        this.providerId = providerId;
    }

    @Contract(pure = true)
    public String getProviderId() {
        return providerId;
    }

}
