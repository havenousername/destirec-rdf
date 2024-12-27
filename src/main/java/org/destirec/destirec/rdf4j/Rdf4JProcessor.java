package org.destirec.destirec.rdf4j;

import org.destirec.destirec.rdf4j.model.ModelRDF;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemProcessor;

public class Rdf4JProcessor implements ItemProcessor<ModelRDF, String> {
    @NotNull
    @Override
    public String process(ModelRDF modelRDF) {
        return "String";
    }
}
