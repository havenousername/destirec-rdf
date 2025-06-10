package org.destirec.destirec.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.eclipse.rdf4j.model.IRI;

import java.io.IOException;

public class IriSerializer extends StdSerializer<IRI> {
    public IriSerializer() {
        super(IRI.class);
    }
    @Override
    public void serialize(IRI value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.stringValue());  // or whatever string representation you want
    }
}
