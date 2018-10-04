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

	/* Regex split query into tokens */
	private String mRegex;
	/* How to define a Literal (Everything which is not not a literal) */
	private String mLiteralRegex;
	/* Stack of operators */
	private Stack<String> mOperator = new Stack<String>();
	/* Stack of components */
	private Stack<QueryComponent> mComponents = new Stack<QueryComponent>();

	/**
	 * Public Constructor no params needed
	 */
	public BooleanQueryParser() {
		mLiteralRegex = "[^ \"\\(\\)\\+\\[\\]\\/]+";
		mRegex = "(\")|(\\()|(\\))| (\\-)|(\\+)|(\\[)|(\\])|(NEAR\\/[0-9]+)|(" + mLiteralRegex + ")";
	}

	/**
	 * Parse a query :
	 * - split it among token
	 * - read token by token
	 * - compress / eval by pop and push in the stack
	 * - eval the final stack
	 */
	public QueryComponent parseQuery(String query) {
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
			if (item.matches(mLiteralRegex)) {
				if (lastEntryLiteral) { addOperator("AND", insideLiteral, lastEntryLiteral); }
				mComponents.push(new TermLiteral(item.trim()));
				lastEntryLiteral = true;
			} else {
				if (addOperator(item.trim(), insideLiteral, lastEntryLiteral)) { lastEntryLiteral = false; }
				if (item.equals("\"")) { insideLiteral = !insideLiteral; }
			}
		}
		parseTree();
		return mComponents.pop();
	}

	/**
	 * Eval the stack
	 */
	private void parseTree() {
		String op = null;
		while (mOperator.size() > 0 && mComponents.size() > 0) {
			op = mOperator.pop();
			mComponents.push(parseFunction(mComponents.pop(), op, mComponents.pop()));
		}
	}

	/**
	 * Add an operator to the operator stack / eval / compress when needed
	 */
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

	/**
	 * Parse each function supported
	 */
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
}
