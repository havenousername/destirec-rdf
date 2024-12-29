package org.destirec.destirec.rdf4j.dao.user;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.spring.dao.SimpleRDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.opbuilder.UpdateExecutionBuilder;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



@Repository
public class UserDao extends SimpleRDF4JCRUDDao<UserDto, IRI> {
    private final UserMigration migration;
    private final UserModel userModel;
    public UserDao(
            RDF4JTemplate rdf4JTemplate,
            UserModel model,
            UserMigration userMigration
    ) {
        super(rdf4JTemplate);
        userModel = model;
        this.migration = userMigration;
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(NamedSparqlSupplierPreparer namedSparqlSupplierPreparer) {
        return null;
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(userModel.getId(), iri);
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, UserDto userDto) {
        List<String> userDtoList = userDto.getList();
        AtomicInteger index = new AtomicInteger();
        userModel.getVariableNames().forEach((field, variable) -> {
            bindingsBuilder
                    .add(variable, userDtoList.get(index.get()));
            index.getAndIncrement();
        });
    }

    @Override
    protected UserDto mapSolution(BindingSet querySolution) {
        return new UserDto(
                QueryResultUtils.getIRI(querySolution, userModel.getId()),
                QueryResultUtils.getString(querySolution, userModel.getVariable(UserModel.Fields.NAME)),
                QueryResultUtils.getString(querySolution, userModel.getVariable(UserModel.Fields.USERNAME)),
                QueryResultUtils.getString(querySolution, userModel.getVariable(UserModel.Fields.EMAIL)),
                QueryResultUtils.getString(querySolution, userModel.getVariable(UserModel.Fields.OCCUPATION))
        );
    }


//    @Override
//    protected NamedSparqlSupplier getUpdateSparql(UserDto userDto) {
//        return super.getUpdateSparql(userDto);
//    }


//    @Override
//    protected NamedSparqlSupplier getInsertSparql(UserDto userDto) {
//        return super.getInsertSparql(userDto);
//    }

    @Override
    protected String getReadQuery() {
        return Queries.SELECT(
                userModel.getId(),
                userModel.getVariable(UserModel.Fields.NAME),
                userModel.getVariable(UserModel.Fields.EMAIL)
        )
                .where(
                        userModel.getId()
                                .isA(migration.get())
                                .andHas(userModel.getPredicate(UserModel.Fields.NAME), userModel.getVariable(UserModel.Fields.NAME))
                                .andHas(userModel.getPredicate(UserModel.Fields.EMAIL), userModel.getVariable(UserModel.Fields.EMAIL))
                )
                .getQueryString();
    }


    @Override
    protected UpdateExecutionBuilder getNamedUpdate(String key) {
        return super.getNamedUpdate(key);
    }

    @Override
    protected IRI getInputId(UserDto userDto) {
        if (userDto.id() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return userDto.id();
    }
}
