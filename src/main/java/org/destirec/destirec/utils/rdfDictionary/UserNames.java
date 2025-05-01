package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class UserNames {
    private UserNames() {}


    public final static class Classes {
        public final static String USER = DESTIREC.wrapNamespace("User");
        public final static String USER_WITH_PREFERENCE = DESTIREC.wrapNamespace("UserWithPreference");
    }
}
