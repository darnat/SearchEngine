package cecs429.query;

import cecs429.text.*;
import cecs429.query.*;

import java.lang.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Parses boolean queries according to the base requirements of the CECS 429
 * project. Does not handle phrase queries, NOT queries, NEAR queries, or
 * wildcard queries... yet.
 */
public class BooleanQueryParser {

	private String mRegex;
	private Stack<String> mOperator = new Stack<String>();
	private Stack<QueryComponent> mComponents = new Stack<QueryComponent>();

	public BooleanQueryParser() {
		mRegex = "([a-zA-Z0-9]+\\-?[a-zA-Z0-9]+)|(\")|(\\()|(\\))|(\\-)|(\\+)|(\\[)|(\\])|(NEAR\\/[0-9]+)";
	}

	// Check if regex if valid
	public QueryComponent parseQuery3(String query) {
		Pattern p = Pattern.compile(mRegex);
		Boolean lastEntryLiteral = false;
		Boolean insideLiteral = false;
		Boolean opendPhrase = false;
		Matcher m;
		String item;

		query = query.trim();
		m = p.matcher(query);
		while (m.find()) {
			item = m.group(0);
			
			if (item.matches("[a-zA-Z0-9]+\\-?[a-zA-Z0-9]+")) {
				if (lastEntryLiteral) { addOperator("AND", insideLiteral, lastEntryLiteral); }
				mComponents.push(new TermLiteral(item));
				lastEntryLiteral = true;
			} else {
				if (addOperator(item, insideLiteral, lastEntryLiteral)) { lastEntryLiteral = false; }
				if (item.equals("\"")) { insideLiteral = !insideLiteral; }
			}
		}
		parseTree();
		// System.out.println("Print Stack ----------------------");
		// System.out.println(Arrays.toString(mOperator.toArray()));
		// System.out.println(Arrays.toString(mComponents.toArray()));
		// System.out.println("---------------------- Print Stack");
		// System.exit(0);
		return mComponents.pop();
	}

	private void parseTree() {
		String op = null;
		while (mOperator.size() > 0 && mComponents.size() > 0) {
			op = mOperator.pop();
			mComponents.push(parseFunction(mComponents.pop(), op, mComponents.pop()));
		}
	}

	private boolean addOperator(String item, Boolean insideLiteral, Boolean lastEntryLiteral) {
		String op = null;
		List<String> terms;

		if (item.equals(")")) {
			while (mOperator.size() > 0 && !(op = mOperator.pop()).equals("(") && mComponents.size() > 0) {
				mComponents.push(parseFunction(mComponents.pop(), op, mComponents.pop()));
			}
		} else if (item.equals("\"") && insideLiteral) {
			terms = new ArrayList<String>();
			terms.add(0, ((TermLiteral)mComponents.pop()).getTerm());
			while (mOperator.size() > 0 && !(op = mOperator.pop()).equals("\"") && mComponents.size() > 0) {
				terms.add(0, ((TermLiteral)mComponents.pop()).getTerm());
			}
			mComponents.push(new PhraseLiteral(terms));
		} else if (item.equals("]")) {
			terms = new ArrayList<String>();
			while (mOperator.size() > 0 && !(op = mOperator.pop()).equals("[") && mComponents.size() > 0) {
				terms.add(0, ((TermLiteral)mComponents.pop()).getTerm());
				terms.add(0, op);
				terms.add(0, ((TermLiteral)mComponents.pop()).getTerm());
			}
			mComponents.push(new NearLiteral(terms));
		} else {
			if ((item.equals("(") || item.equals("\"") || item.equals("]")) && lastEntryLiteral) { mOperator.push("AND"); }
			mOperator.push(item);
			return true;
		}
		return false;
	}

	private QueryComponent parseFunction(QueryComponent cm1, String operator, QueryComponent cm2) {
		if (operator.equals("AND")) {
			return new AndQuery(cm2, cm1);
		}
		if (operator.equals("+")) {
			return new OrQuery(cm2, cm1);
		}
		if (operator.equals("-")) {
			return new NotQuery(cm2, cm1);
		}
		return null;
	}

	// public QueryComponent parseQuery2(String query) {
	// 	Pattern p = Pattern.compile(mRegex);
	// 	Matcher m;
	// 	boolean lastEntryLiteral = false;
	// 	boolean lastOperator = false;
	// 	boolean phraseLiteral = false;
	// 	String item;

	// 	query = query.trim();
	// 	m = p.matcher(query);
	// 	while (m.find()) {
	// 		item = m.group(0);
	// 		if (item.matches("[a-z0-9]+\\-?[a-z0-9]+")) {
	// 			if (lastEntryLiteral) { mOperator.push("AND"); }
	// 			mComponents.push(new TermLiteral(item));
	// 			lastEntryLiteral = true;
	// 		} else {
	// 			lastEntryLiteral = false;
	// 			mOperator.push(item);
	// 		}

	// 		if (mOperator.size() == 0) { continue; }
	// 		if (!lastEntryLiteral && mOperator.peek().equals(")")) {
	// 			lastOperator = true;
	// 			mOperator.pop();
	// 			while (mOperator.size() > 0
	// 					&& mComponents.size() > 0
	// 					&& !((item = mOperator.pop()).equals("("))) {
	// 						mComponents.push(parseFunction(mComponents.pop(), item, mComponents.pop()));
	// 					}
	// 		}
	// 		if (!lastEntryLiteral && mOperator.peek().equals("\"")) {
	// 			mOperator.pop();
	// 			if (phraseLiteral) {
	// 				List<String> terms = new ArrayList<String>();
	// 				while (mOperator.size() > 0
	// 				&& mComponents.size() > 0
	// 				&& !(mOperator.pop().equals("\""))) {
	// 					terms.add(((TermLiteral)(mComponents.pop())).getTerm());
	// 				}
	// 				terms.add(0, ((TermLiteral)(mComponents.pop())).getTerm());
	// 				mComponents.push(new PhraseLiteral(terms));
	// 				phraseLiteral = false;
	// 			} else {
	// 				phraseLiteral = true;
	// 			}
	// 		}
	// 	}

	// 	while (mOperator.size() > 0
	// 			&& mComponents.size() > 0) {
	// 				item = mOperator.pop();
	// 				mComponents.push(parseFunction(mComponents.pop(), item, mComponents.pop()));
	// 			}
	// 	// parseTree(mOperator, mComponents);
	// 	// System.out.println("|"+query+"|");
	// 	// for (String recognizer: mOperatorToken) {
	// 	// 	p = Pattern.compile(recognizer);
	// 	// 	m = p.matcher(query);
	// 	// 	if (m.find()) {
	// 	// 		System.out.println("Matched Query : " + m.group(1));
	// 	// 		query = query.substring(m.group(1).length());
	// 	// 		mOperator.add(recognizer);
	// 	// 		return parseQuery(query);
	// 	// 	}
	// 	// }
	// 	// System.out.println("final query : " + query);
	// 	return mComponents.pop();
	// }



	/**
	 * Identifies a portion of a string with a starting index and a length.
	 */
	private static class StringBounds {
		int start;
		int length;
		
		StringBounds(int start, int length) {
			this.start = start;
			this.length = length;
		}
	}
	
	/**
	 * Encapsulates a QueryComponent and the StringBounds that led to its parsing.
	 */
	private static class Literal {
		StringBounds bounds;
		QueryComponent literalComponent;
		
		Literal(StringBounds bounds, QueryComponent literalComponent) {
			this.bounds = bounds;
			this.literalComponent = literalComponent;
		}
	}
	
	/**
	 * Given a boolean query, parses and returns a tree of QueryComponents representing the query.
	 */
	// public QueryComponent parseQuery(String query) {
	// 	int start = 0;
		
	// 	// General routine: scan the query to identify a literal, and put that literal into a list.
	// 	//	Repeat until a + or the end of the query is encountered; build an AND query with each
	// 	//	of the literals found. Repeat the scan-and-build-AND-query phase for each segment of the
	// 	// query separated by + signs. In the end, build a single OR query that composes all of the built
	// 	// AND subqueries.
		
	// 	List<QueryComponent> allSubqueries = new ArrayList<>();
	// 	do {
	// 		// Identify the next subquery: a portion of the query up to the next + sign.
	// 		StringBounds nextSubquery = findNextSubquery(query, start);
	// 		// Extract the identified subquery into its own string.
	// 		String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);
	// 		int subStart = 0;
			
	// 		// Store all the individual components of this subquery.
	// 		List<QueryComponent> subqueryLiterals = new ArrayList<>(0);

	// 		do {
	// 			// Extract the next literal from the subquery.
	// 			Literal lit = findNextLiteral(subquery, subStart);
				
	// 			// Add the literal component to the conjunctive list.
	// 			subqueryLiterals.add(lit.literalComponent);
				
	// 			// Set the next index to start searching for a literal.
	// 			subStart = lit.bounds.start + lit.bounds.length;
				
	// 		} while (subStart < subquery.length());
			
	// 		// After processing all literals, we are left with a conjunctive list
	// 		// of query components, and must fold that list into the final disjunctive list
	// 		// of components.
			
	// 		// If there was only one literal in the subquery, we don't need to AND it with anything --
	// 		// its component can go straight into the list.
	// 		if (subqueryLiterals.size() == 1) {
	// 			allSubqueries.add(subqueryLiterals.get(0));
	// 		} else {
	// 			// With more than one literal, we must wrap them in an AndQuery component.
	// 			allSubqueries.add(new AndQuery(subqueryLiterals));
	// 		}
	// 		start = nextSubquery.start + nextSubquery.length;
	// 	} while (start < query.length());
		
	// 	// After processing all subqueries, we either have a single component or multiple components
	// 	// that must be combined with an OrQuery.
	// 	if (allSubqueries.size() == 1) {
	// 		return allSubqueries.get(0);
	// 	} else if (allSubqueries.size() > 1) {
	// 		return new OrQuery(allSubqueries);
	// 	} else {
	// 		return null;
	// 	}
	// }
	
	/**
	 * Locates the start index and length of the next subquery in the given query string,
	 * starting at the given index.
	 */
	// private StringBounds findNextSubquery(String query, int startIndex) {
	// 	int lengthOut;
		
	// 	// Find the start of the next subquery by skipping spaces and + signs.
	// 	char test = query.charAt(startIndex);
	// 	while (test == ' ' || test == '+') {
	// 		test = query.charAt(++startIndex);
	// 	}
		
	// 	// Find the end of the next subquery.
	// 	int nextPlus = query.indexOf('+', startIndex + 1);
		
	// 	if (nextPlus < 0) {
	// 		// If there is no other + sign, then this is the final subquery in the
	// 		// query string.
	// 		lengthOut = query.length() - startIndex;
	// 	} else {
	// 		// If there is another + sign, then the length of this subquery goes up
	// 		// to the next + sign.
		
	// 		// Move nextPlus backwards until finding a non-space non-plus character.
	// 		test = query.charAt(nextPlus);
	// 		while (test == ' ' || test == '+') {
	// 			test = query.charAt(--nextPlus);
	// 		}
			
	// 		lengthOut = 1 + nextPlus - startIndex;
	// 	}
		
	// 	// startIndex and lengthOut give the bounds of the subquery.
	// 	return new StringBounds(startIndex, lengthOut);
	// }
	
	/**
	 * Locates and returns the next literal from the given subquery string.
	 */
	// private Literal findNextLiteral(String subquery, int startIndex) {
	// 	int subLength = subquery.length();
	// 	int lengthOut;

	// 	// Skip past white space.
	// 	while (subquery.charAt(startIndex) == ' ') {
	// 		++startIndex;
	// 	}

	// 	if (subquery.substring(startIndex).startsWith("\"")) {
	// 		List<String> terms = new ArrayList<>();
	// 		// Find next double quote
	// 		int nextDoubleQuote = subquery.indexOf('"', startIndex + 1);
	// 		lengthOut = nextDoubleQuote - startIndex + 1;
	// 		// Add terms to array without the double quotes
	// 		terms.addAll(Arrays.asList(subquery.substring(startIndex + 1, nextDoubleQuote).split(" ")));

	// 		System.out.println("Phrase Literal found: " + subquery.substring(startIndex + 1, nextDoubleQuote));

	// 		return new Literal(
	// 			new StringBounds(startIndex, lengthOut),
	// 			new PhraseLiteral(terms)
	// 		);
	// 	} else if (subquery.substring(startIndex).startsWith("[")) {
	// 		List<String> terms = new ArrayList<>();
	// 		// Find closing bracket
	// 		int closingBracketIndex = subquery.indexOf(']', startIndex + 1);
	// 		lengthOut = closingBracketIndex - startIndex + 1;
                        
	// 		// Add terms to array without the double quotes
	// 		terms.addAll(Arrays.asList(subquery.substring(startIndex + 1, closingBracketIndex).split(" ")));

	// 		return new Literal(
	// 			new StringBounds(startIndex, lengthOut),
	// 			new NearLiteral(terms)
	// 		);
	// 	} else if (subquery.substring(startIndex).startsWith("-")) {
	// 		// Extract next literal
	// 		Literal lit = findNextLiteral(subquery, startIndex + 1);
	// 		System.out.println("NOT query found: " + subquery.substring(startIndex, lit.bounds.start + lit.bounds.length));
	// 		return new Literal(
	// 			new StringBounds(startIndex, lit.bounds.start + lit.bounds.length),
	// 			new NotQuery(lit.literalComponent)
	// 		);
	// 	} else {		
	// 		// Locate the next space to find the end of this literal.
	// 		int nextSpace = subquery.indexOf(' ', startIndex);
			
	// 		if (nextSpace < 0) {
	// 			// No more literals in this subquery.
	// 			lengthOut = subLength - startIndex;
	// 		} else {
	// 			lengthOut = nextSpace - startIndex;
	// 		}

	// 		//System.out.println("Literal found: " + subquery.substring(startIndex, startIndex + lengthOut));                 
                        
	// 		// if * is present, then the literal is a wildcard
	// 		if (subquery.substring(startIndex, startIndex + lengthOut).contains("*")) {
	// 			return new Literal(
	// 				new StringBounds(startIndex, lengthOut),
	// 				new WildCardLiteral(subquery.substring(startIndex, startIndex + lengthOut))
	// 			);
	// 		} else {
	// 			System.out.println("Term Literal found: " + subquery.substring(startIndex, startIndex + lengthOut));
	// 			// This is a term literal containing a single term.
	// 			return new Literal(
	// 				new StringBounds(startIndex, lengthOut),
	// 				new TermLiteral(subquery.substring(startIndex, startIndex + lengthOut))
	// 			);
	// 		}
	// 	}
	// }
}
