package org.destirec.destirec.rdf4j.user.apiDto;

public record CreateUserDto(
        String id,
        String name,
        String username,
        String email,
        String occupation
) {}
