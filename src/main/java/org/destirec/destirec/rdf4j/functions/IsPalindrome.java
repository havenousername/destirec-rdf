package org.destirec.destirec.rdf4j.functions;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

import static org.eclipse.rdf4j.model.util.Values.literal;

public class IsPalindrome implements Function {
    @Override
    public String getURI() {
        return DESTIREC.NAMESPACE + "resource/function/palindrome";
    }

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        // our palindrome function expects only a single argument, so throw an error
        // if there's more than one
        if (args.length != 1) {
            throw new ValueExprEvaluationException(
                    "palindrome function requires"
                            + "exactly 1 argument, got "
                            + args.length);
        }
        boolean palindrome = isPalindrome(args);

        // a function is always expected to return a Value object, so we
        // return our boolean result as a Literal
        return literal(palindrome);
    }

    private static boolean isPalindrome(Value[] args) {
        Value arg = args[0];
        // check if the argument is a literal, if not, we throw an error
        if (!(arg instanceof Literal)) {
            throw new ValueExprEvaluationException(
                    "invalid argument (literal expected): " + arg);
        }

        // get the actual string value that we want to check for palindrome-ness.
        String label = ((Literal)arg).getLabel();
        // we invert our string
        StringBuilder inverted = new StringBuilder();
        for (int i = label.length() - 1; i >= 0; i--) {
            inverted.append(label.charAt(i));
        }
        // a string is a palindrome if it is equal to its own inverse
        return inverted.toString().equalsIgnoreCase(label);
    }

    @Override
    public boolean mustReturnDifferentResult() {
        return Function.super.mustReturnDifferentResult();
    }
}
