/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.metadata.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MatchResult;
import org.springframework.metadata.AttributeParser;

/**
 * This class parses the attribute text applied to a program element  
 * and returns the results of parsing in the utility class AttributeDefinition. 
 * The syntax for applying attributes to program elements follows the .NET
 * style.
 * 
 * The attribute classname and any necessary parameters are placed in Javadoc
 * style comments of the target element.  Attribute parameters can either be
 * positional or named.  Positional parameters are comma separated list of
 * the parameters to used for any declared constructor.  They must be in the 
 * correct order and appear before any named parameters.  Named parameters are
 * comma separated name/value pairs.  They must appear after positional parameters
 * and their order is ont important.  
 * 
 * In code this looks something like
 * <code>
 *  PART I                PART II
 * @AttributeClassName ( ctorarg1, ctorarg2, propName1=propValue1, propName2=propValue2)
 * </code>
 *
 * Be careful, the parsing code is not incredibly robust at the moment, but it should
 * be sufficient for most needs.
 * 
 * 
 * @author Mark Pollack
 * @since Sep 28, 2003
 * 
 */
public class DotNetAttributeParser implements AttributeParser {

	private static Perl5Util perl = new Perl5Util();

	public DotNetAttributeParser() {

	}

	/**
	 * {@inheritdoc}
	 * @param attributeText {@inheritdoc}
	 */
	public AttributeDefinition getAttributeDefinition(final String attributeText) {
		String className;

		if (attributeText == null) {
			return new AttributeDefinition(
				attributeText,
				"Attribute Text was null");
		}

		//Find the attribute classname.
		if (attributeText.indexOf("(") > -1) {
			//found a starting parenthesis, so get everything before it.
			className =
				attributeText.substring(0, attributeText.indexOf("(")).trim();
		} else {
			//no starting parenthesis, nothing left to do.
			className = attributeText.trim();
			
			return new AttributeDefinition(
				attributeText,
				true,
				className,
				new ArrayList(),
				new HashMap());
		}

		//Parse out the ctor args and property names based on a simple
		//comma delimited syntax.
		//TODO use a more robust regexp technique.
		int paramsStart = attributeText.indexOf("(");
		int paramsEnd = attributeText.lastIndexOf(")");
		if (paramsEnd == -1) {
			return new AttributeDefinition(
				attributeText,
				"Could not find closing parenthesis in attribute text = <"
					+ attributeText
					+ ">");
		}
		String paramText = attributeText.substring(paramsStart + 1, paramsEnd);

		List paramList = new ArrayList();
		perl.split(paramList, "/,/", paramText);
		//System.out.println("Parameters = " + paramList);

		//convert to array
		String[] param =
			(String[]) paramList.toArray(new String[paramList.size()]);
		int count = param.length;

		//allocate storage
		List ctorArgs = new ArrayList();
		Map propMap = new HashMap();

		//match region like
		// (spaces) (word) (spaces) = (anything)
		String regexp = "/\\s*(\\w+)\\s*=(.*)/";
		int propIndex = -1;
		for (int i = 0; i < count; i++) {
			if (perl.match(regexp, param[i])) {
				//if (param[i].indexOf("=") != -1){
				//Found start of property list
				propIndex = i;
				break;
			}
		}
		if (propIndex == -1) {
			//no properties found.
			for (int i = 0; i < count; i++) {
				ctorArgs.add(param[i].trim());
			}
		} else {
			//properties found
			for (int i = 0; i < propIndex; i++) {
				ctorArgs.add(param[i].trim());
			}
			for (int i = propIndex; i < count; i++) {
				if (perl
					.match(
						regexp,//    "/\\s*(\\w+)\\s*=\\s*(\\S+)\\s*/",
						param[i].trim())) {
					MatchResult matchResult = perl.getMatch();
					/**
					System.out.println(
						"match property name/value pair<"
							+ matchResult
							+ ">, # groups =	"
							+ matchResult.groups());
					*/
					if (matchResult.groups() == 3) {
						//System.out.println("group 1 = " + matchResult.group(1));
						//System.out.println("group 2 = " + matchResult.group(2));
						String propName = matchResult.group(1).trim();
						String propValue = matchResult.group(2).trim();
						propMap.put(propName, propValue);
					} else {
						return new AttributeDefinition(
							attributeText,
							"Could not parse name/value property value =<"
								+ matchResult.group(0)
								+ "> for attribute text = <"
								+ attributeText
								+ ">");
					}
				} else {
					return new AttributeDefinition(
						attributeText,
						"Could not parse name/value property <"
							+ param[i].trim()
							+ ">"
							+ " for attribute text = <"
							+ attributeText
							+ ">");
				}
			}
		}
		return new AttributeDefinition(
			attributeText,
			true,
			className,
			ctorArgs,
			propMap);

	}

}
