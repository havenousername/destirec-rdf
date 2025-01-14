package org.destirec.destirec.rdf4j.user.apiDto;

public record ExternalUserDto(
        String id,
        String name,
        String username,
        String email,
        String occupation
) {}
