package org.destirec.destirec.rdf4j.dao.user;

import org.eclipse.rdf4j.model.IRI;

import java.util.List;

public record UserDto(
        IRI id,
        String name,
        String username,
        String email,
        String occupation
) {
   public List<String> getList() {
       return List.of(name, username, email, occupation);
   }
}
