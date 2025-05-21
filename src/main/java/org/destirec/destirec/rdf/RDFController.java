package org.destirec.destirec.rdf;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rdf")
public class RDFController {

    private final RDF4JTemplate rdf4JTemplate;
    public RDFController(RDF4JTemplate rdf4JTemplate) {
        this.rdf4JTemplate = rdf4JTemplate;
    }

    @GetMapping("/turtle")
    public ResponseEntity<String> getTurtle() {
        String output = rdf4JTemplate.applyToConnection(conn -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            conn.export(Rio.createWriter(RDFFormat.TURTLE, out));
            return out.toString(StandardCharsets.UTF_8);
        });
        return ResponseEntity.ok(output);
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeRdfCommand(@RequestBody Map<String, String> request) {
        String command = request.get("command");

        try {
            Map<String, Object> apiResponse = rdf4JTemplate.applyToConnection(connection -> {
                var startsWith = command.trim();
                Map<String, Object> response = new HashMap<>();
                if (startsWith.toUpperCase().startsWith("SELECT")) {
                    TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, command);
                    try (TupleQueryResult result = tupleQuery.evaluate()){
                        List<Map<String, String>> results = new ArrayList<>();

                        while (result.hasNext()) {
                            BindingSet bindings = result.next();
                            Map<String, String> row = new HashMap<>();

                            for (Binding binding : bindings) {
                                row.put(binding.getName(), binding.getValue().stringValue());
                            }

                            results.add(row);
                        }
                        response.put("results", results);
                    }
                } else if (startsWith.toUpperCase().startsWith("INSERT") || command.toUpperCase().startsWith("DELETE")) {
                    Update update = connection.prepareUpdate(QueryLanguage.SPARQL, command);
                    update.execute();
                    response.put("message", "Update executed successfully");
//                    destiRecOntology.triggerInference();
                } else {
                    // other types like CONSTRUCT etc.
                    GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, command);
                    try (GraphQueryResult result= graphQuery.evaluate()) {
                        List<Map<String, String>> results = new ArrayList<>();
                        while (result.hasNext()) {
                            Statement stmt = result.next();
                            Map<String, String> triple = new HashMap<>();
                            triple.put("subject", stmt.getSubject().stringValue());
                            triple.put("predicate", stmt.getPredicate().stringValue());
                            triple.put("object", stmt.getObject().stringValue());
                            results.add(triple);
                        }
                        response.put("results", results);
                        response.put("message", "Query executed successfully");
                    }
                }

                return response;
            });
            return ResponseEntity.ok(apiResponse);

        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to execute command " + exception.getMessage()));
        }
    }
}
