package de.datlag.hotdrop.auth

import com.google.gson.Gson
import com.google.gson.JsonObject
import lombok.Getter
import lombok.Setter

class InteraAccount(response: String) {
    private var `object`: JsonObject = Gson().fromJson(response, JsonObject::class.java)
    private var error = true
    var username: String? = null
    var email: String? = null
    var description: String? = null
    var verified = false

    init {
        error = `object`.get("error").asBoolean
        if (!error) {
            username = `object`.get("username").asString
            email = `object`.get("username").asString
            description = `object`.get("description").asString
            verified = `object`.get("verified").asBoolean
        }
    }
}