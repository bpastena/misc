package org.mposolda.drools.uripolicytest;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderErrors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * Hello world!
 *
 */
public class UriPolicyTrigger {

    public static void main( String[] args ) throws Exception {
        RuleBase ruleBase = initDrools();

        WorkingMemory workingMemory = ruleBase.newStatefulSession();

        Result result = new Result();
        workingMemory.insert(result);

        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("param1", "value1");
        reqParams.put("param2", "value2");
        UriPolicyInput uriInput = new UriPolicyInput("/kokos/mlok", reqParams);
        workingMemory.insert(uriInput);

        List<String> roles =  Arrays.asList(new String[] {"mlok", "kolok", "bar"});
        Token token = new Token("mlok", roles);
        workingMemory.insert(token);

        MatcherInfo mi = new MatcherInfo();
        workingMemory.insert(mi);

        int numberOfFiredPolicies = workingMemory.fireAllRules();
        System.out.println("numberOfFiredPolicies=" + numberOfFiredPolicies + ", rules=" + result.getDecision());
    }

    private static RuleBase initDrools() throws IOException, DroolsParserException {
        PackageBuilder packageBuilder = new PackageBuilder();
        String ruleFile = "/org/mposolda/drools/uripolicytest/uriPolicyTest.drl";
        InputStream resourceAsStream = UriPolicyTrigger.class.getResourceAsStream(ruleFile);
        Reader reader = new InputStreamReader(resourceAsStream);
        packageBuilder.addPackageFromDrl(reader);

        PackageBuilderErrors errors = packageBuilder.getErrors();
        if (errors.getErrors().length > 0) {
            StringBuilder errorMessages = new StringBuilder();
            errorMessages.append("Found errors in package builder\n");
            for (int i = 0; i < errors.getErrors().length; i++) {
                DroolsError errorMessage = errors.getErrors()[i];
                errorMessages.append(errorMessage);
                errorMessages.append("\n");
            }
            errorMessages.append("Could not parse knowledge");

            throw new IllegalArgumentException(errorMessages.toString());
        }

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        org.drools.rule.Package rulesPackage = packageBuilder.getPackage();
        ruleBase.addPackage(rulesPackage);
        return ruleBase;
    }
}