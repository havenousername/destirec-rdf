package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalUserDto;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserDtoCreator implements DtoCreator<UserDto, UserConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final UserConfig userConfig;

    public UserDtoCreator(UserConfig userConfig) {
        this.userConfig = userConfig;
    }

    @Override
    public UserDto create(IRI id, Map<UserConfig.Fields, String> map) {
        return new UserDto(
                id,
                map.get(UserConfig.Fields.NAME),
                map.get(UserConfig.Fields.USERNAME),
                map.get(UserConfig.Fields.EMAIL),
                map.get(UserConfig.Fields.OCCUPATION)
        );
    }

    public UserDto create(ExternalUserDto externalUserDto) {
        return new UserDto(
                createId(externalUserDto.id()),
                externalUserDto.name(),
                externalUserDto.username(),
                externalUserDto.email(),
                externalUserDto.occupation()
        );
    }

    public IRI createId(String id) {
        return valueFactory.createIRI(userConfig.getResourceLocation() + id);
    }

    public UserDto create(String id, ExternalUserDto externalUserDto) {
        return new UserDto(
                createId(id),
                externalUserDto.name(),
                externalUserDto.username(),
                externalUserDto.email(),
                externalUserDto.occupation()
        );
    }

    @Override
    public UserDto create(Map<UserConfig.Fields, String> map) {
        return create(null, map);
    }
}
