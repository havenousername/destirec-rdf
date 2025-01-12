package org.destirec.destirec.rdf4j.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Map;

public record UserDto(
        @NonNull
        IRI id,
        @Nullable
        String name,
        @NonNull
        String username,

        @NonNull
        String email,

        @Nullable
        String occupation
) implements Dto, Serializable {
    @JsonCreator
    public UserDto(
            @JsonProperty("id") IRI id,
            @JsonProperty("name") String name,
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("occupation") String occupation
    ) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.occupation = occupation;
    }

   public Map<ModelFields.Field, String> getMap() {
        Map<ModelFields.Field, String> requiredFields = new java.util.HashMap<>(Map.ofEntries(
                Map.entry(UserModel.Fields.USERNAME, username),
                Map.entry(UserModel.Fields.EMAIL, email)
        ));
        if (name != null) {
            requiredFields.put(UserModel.Fields.NAME, username);
        }

        if (occupation != null) {
            requiredFields.put(UserModel.Fields.OCCUPATION, occupation);
        }
        return requiredFields;
   }

   public UserDto(String name, String username, String email, String occupation) {
       this(null, name, username, email, occupation);
   }
}
