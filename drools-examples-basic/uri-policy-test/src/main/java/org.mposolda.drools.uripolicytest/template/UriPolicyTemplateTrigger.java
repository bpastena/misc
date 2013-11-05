package org.mposolda.drools.uripolicytest.template;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsError;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderErrors;
import org.drools.template.DataProvider;
import org.drools.template.DataProviderCompiler;
import org.mposolda.drools.uripolicytest.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UriPolicyTemplateTrigger {

    public static void main(String[] args) throws Exception {
        UriTemplate template1 = new UriTemplate(8, "\"^/something/amos$\"", "reqParams.get(\"param1\") == \"value1\"");
        UriTemplate template2 = new UriTemplate(10, "\"^/something/([abc].*)$\"", "reqParams.get(\"param1\") == \"value1\"");
        List<UriTemplate> uriTemplates = new ArrayList<UriTemplate>();
        uriTemplates.add(template1);
        uriTemplates.add(template2);

        String template = buildTemplate(uriTemplates);
        System.out.println(template);


        RuleBase ruleBase = initDrools(template);

        WorkingMemory workingMemory = ruleBase.newStatefulSession();

        Result result = new Result();
        workingMemory.insert(result);

        EndChecker endChecker = new EndChecker();
        workingMemory.insert(endChecker);

        Map<String, String> reqParams = new HashMap<String, String>();
        reqParams.put("param1", "value1");
        reqParams.put("param2", "value2");
        UriPolicyInput uriInput = new UriPolicyInput("/something/amos", reqParams);
        workingMemory.insert(uriInput);

        List<String> roles =  Arrays.asList(new String[]{"mlok", "kolok", "bar"});
        Token token = new Token("mlok", roles);
        workingMemory.insert(token);

        MatcherInfo mi = new MatcherInfo();
        workingMemory.insert(mi);

        int numberOfFiredPolicies = workingMemory.fireAllRules();
        System.out.println("numberOfFiredPolicies=" + numberOfFiredPolicies + ", rules=" + result.getDecision());

    }

    private static String buildTemplate(List<UriTemplate> uriTemplates) {
        InputStream templateStream = UriPolicyTemplateTrigger.class.getResourceAsStream("uriPolicyTemplateTest.drl");
        UriTemplateDataProvider tdp = new UriTemplateDataProvider(uriTemplates.iterator());
        DataProviderCompiler converter = new DataProviderCompiler();
        final String drl = converter.compile(tdp, templateStream);
        return drl;
    }


    private static RuleBase initDrools(String templateString) throws IOException, DroolsParserException {
        PackageBuilder packageBuilder = new PackageBuilder();

        // Add DRL with functions
        InputStream resourceAsStream = UriPolicyTemplateTrigger.class.getResourceAsStream("uriPolicyFunctions.drl");
        Reader reader = new InputStreamReader(resourceAsStream);
        packageBuilder.addPackageFromDrl(reader);

        // Add DRL based on template
        reader = new StringReader(templateString);
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