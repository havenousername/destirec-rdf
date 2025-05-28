package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DC;

public class PreferenceNames {
    private PreferenceNames() {}

    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper PREFERENCE = DESTIREC.wrap("Preference");
    }

    public final static class Properties {
        public final static IRI PREFERENCE_AUTHOR = DC.CREATOR;
    }
}
