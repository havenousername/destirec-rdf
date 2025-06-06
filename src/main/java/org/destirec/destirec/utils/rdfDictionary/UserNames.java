package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class UserNames {
    private UserNames() {}

    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper USER = DESTIREC.wrap("User");
        public final static DESTIREC.NamespaceWrapper USER_WITH_PREFERENCE = DESTIREC.wrap("UserWithPreference");
        public final static DESTIREC.NamespaceWrapper USER_ACTIVITY = DESTIREC.wrap("UserActivity");

        public final static DESTIREC.NamespaceWrapper USER_HISTORY = DESTIREC.wrap("UserHistory");
        public final static DESTIREC.NamespaceWrapper USER_FEEDBACK = DESTIREC.wrap("UserFeedback");

        public final static DESTIREC.NamespaceWrapper USER_INFLUENCE = DESTIREC.wrap("UserInfluence");

        public final static DESTIREC.NamespaceWrapper USER_HISTORY_INFLUENCE = DESTIREC.wrap("UserHistoryInfluence");
        public final static DESTIREC.NamespaceWrapper USER_FEEDBACK_INFLUENCE = DESTIREC.wrap("UserFeedbackInfluence");
    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper HAS_ACTIVITY_OVER_ENTITY = DESTIREC.wrap("hasActivityOverEntity");
        public final static DESTIREC.NamespaceWrapper HAS_ACTIVITY_P_SCORE = DESTIREC.wrap("hasActivityPScore");
        public final static DESTIREC.NamespaceWrapper HAS_INFLUENCE_P_SCORE = DESTIREC.wrap("hasInfluencePScore");
        public final static DESTIREC.NamespaceWrapper HAS_INFLUENCE_C_CONFIDENCE = DESTIREC.wrap("hasActivityCConfidence");

        public final static DESTIREC.NamespaceWrapper HAS_VISITED_ENTITY = DESTIREC.wrap("hasVisitedEntity");
        public final static DESTIREC.NamespaceWrapper HAS_VISITED_P_SCORE = DESTIREC.wrap("hasVisitedPScore");


        public final static DESTIREC.NamespaceWrapper HAS_FEEDBACK_ENTITY = DESTIREC.wrap("hasFeedbackEntity");
        public final static DESTIREC.NamespaceWrapper HAS_FEEDBACK_P_SCORE = DESTIREC.wrap("hasFeedbackPScore");

        public final static DESTIREC.NamespaceWrapper INFLUENCE_FOR_REGION = DESTIREC.wrap("forRegion");
        public final static DESTIREC.NamespaceWrapper INFLUENCE_BY_USER = DESTIREC.wrap("byUser");

        public final static DESTIREC.NamespaceWrapper HAS_USER = DESTIREC.wrap("hasUser");
        public final static DESTIREC.NamespaceWrapper HAS_TIME_FROM = DESTIREC.wrap("hasTimeFrom");
        public final static DESTIREC.NamespaceWrapper HAS_TIME_TO = DESTIREC.wrap("hasTimeFrom");
        public final static DESTIREC.NamespaceWrapper HAS_INFLUENCE = DESTIREC.wrap("hasInfluence");
    }
}
