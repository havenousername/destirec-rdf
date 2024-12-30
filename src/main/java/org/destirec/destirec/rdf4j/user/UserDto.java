package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public record UserDto(
        IRI id,
        String name,
        String username,
        String email,
        String occupation
) implements Dto {
   public Map<ModelFields.Field, String> getMap() {
       return Map.ofEntries(
               Map.entry(UserModel.Fields.NAME, name),
               Map.entry(UserModel.Fields.USERNAME, username),
               Map.entry(UserModel.Fields.EMAIL, email),
               Map.entry(UserModel.Fields.OCCUPATION, occupation)
       );
   }

   public UserDto(String name, String username, String email, String occupation) {
       this(null, name, username, email, occupation);
   }
}
