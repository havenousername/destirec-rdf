package org.destirec.destirec.rdf4j.dao.user;

import org.destirec.destirec.rdf4j.dao.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;

public record UserDto(
        IRI id,
        String name,
        String username,
        String email,
        String occupation
) implements Dto {
   public List<String> getList() {
       return List.of(name, username, email, occupation);
   }

   public UserDto(String name, String username, String email, String occupation) {
       this(null, name, username, email, occupation);
   }
}
