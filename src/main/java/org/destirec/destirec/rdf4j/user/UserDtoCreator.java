package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserDtoCreator implements DtoCreator<UserDto, UserModel.Fields> {

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

    @Override
    public UserDto create(Map<UserModel.Fields, String> map) {
        return create(null, map);
    }
}
