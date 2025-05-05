package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class UserNames {
    private UserNames() {}


    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper USER = DESTIREC.wrap("User");
        public final static DESTIREC.NamespaceWrapper USER_WITH_PREFERENCE = DESTIREC.wrap("UserWithPreference");
    }
}
