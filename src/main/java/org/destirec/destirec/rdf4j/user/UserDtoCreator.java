package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserDtoCreator implements DtoCreator<UserDto, UserModel.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final UserModel userModel;

    public UserDtoCreator(UserModel userModel) {
        this.userModel = userModel;
    }

    @Override
    public UserDto create(IRI id, Map<UserModel.Fields, String> map) {
        return new UserDto(
                id,
                map.get(UserModel.Fields.NAME),
                map.get(UserModel.Fields.USERNAME),
                map.get(UserModel.Fields.EMAIL),
                map.get(UserModel.Fields.OCCUPATION)
        );
    }

    public UserDto create(ExternalUserDto externalUserDto) {
        return new UserDto(
                valueFactory.createIRI(userModel.getResourceLocation() + externalUserDto.id()),
                externalUserDto.name(),
                externalUserDto.username(),
                externalUserDto.email(),
                externalUserDto.occupation()
        );
    }

    @Override
    public UserDto create(Map<UserModel.Fields, String> map) {
        return create(null, map);
    }
}
