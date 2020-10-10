package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";


	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, vals for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariablevals method.
	 * 
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		/**
		 * DO NOT create new vars and arrays - they are already created before being
		 * sent in to this method - you just need to fill them in.

		 **/
		
		Array arr;
		StringTokenizer TokenStr = new StringTokenizer(expr, delims);
		
		String val = "";
		Variable variable;
		
		while (TokenStr.hasMoreTokens()) {
			val = TokenStr.nextToken();//points to next token	
			if (Character.isLetter(val.charAt(0)) == true) {//if a letter	
				
				int p1 = expr.indexOf(val);//ptr index to traverse across individual token
				
				int toklength = val.length();
				
				if (p1 + toklength + 1 > expr.length()) {//if the token is the last token
					vars.add(new Variable(val));
					break;
				} else if (expr.charAt(p1 + toklength) == '[') {//checks to see if array
					arr = new Array(val);//creates new array
					if (!arrays.contains(arr)) {
						arrays.add( new Array(val));//adds to list of arrays if it isnt a duplicate
					} 
				} else { //this assumes it is a variable
					variable= new Variable(val);
					if(!vars.contains(variable)) {//checks duplicate
						vars.add( new Variable(val));
					}
				}
			}
		}
	}

	/**
	 * Loads vals for variables and arrays in the expression
	 * 
	 * @param sc Scanner for vals input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { 
				vars.get(vari).value = num;
			} else { // array 
				arr = arrays.get(arri);
				arr.values = new int[num];
				// (index,val)
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @param 
	 * @param 
	 * @return 
	 */
	
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		/** COMPLETE THIS METHOD **/
		// following line just a placeholder for compilation
		ArrayList<String> ops = new ArrayList<String>(Arrays.asList(expr.split("(?<=\\*)|(?=\\*)|(?<=[()])|(?=[()])|(?<=\\])|(?=\\])|(?<=\\[)|(?=\\[)|(?<=\\+)|(?=\\+)|(?=-)|(?<=/)|(?=/)|(?<=-)")));
		//takes every possible situation into account
		String Tokptr = "";
		int p1 = -1;
		int count = 0;//count 
		while (count < ops.size()) {//as long as we havent gone thru all the operators
			if (vars.contains(new Variable(ops.get(count)))) {
				ops.set(count, Integer.toString(vars.get(vars.indexOf(new Variable(ops.get(count)))).value));
			}
			count++;
		}
		while (ops.contains("[") || ops.contains("]")) {
			Tokptr = "";
			for (int i = ops.size() - 1; !ops.get(i).equals("["); i--) {
				p1 = i;
			}
			p1--;
			for (int j = p1 + 1; !ops.get(j).equals("]"); j++) {
				Tokptr = Tokptr.concat(ops.remove(j));
				j--;
			}
			ops.remove(p1);
			ops.remove(p1);
			ops.set(p1 - 1, Integer.toString(
					arrays.get(arrays.indexOf(new Array(ops.get(p1 - 1)))).values[(int) pemdas(Tokptr)]));
		}
		Tokptr = "";
		count =0;
		while(count < ops.size()) {
			Tokptr = Tokptr.concat(ops.get(count));
			count++;
		}
		return pemdas(Tokptr);
	}
	
	
	private static boolean opPriority(char s1, char s2) {
		if (s2 == ')'||s2 == '(') {
			return false;}
		if ((s1 == '/' || s1 == '*') && (s2 == '-'|| s2 == '+')) {
			return false;}
		else {
			return true;}
	}


	private static float pemdas(String expr) {
		
		Stack<Float> nums = new Stack<Float>();
		Stack<Character> ops = new Stack<Character>();
		
		char[] charray = expr.toCharArray();
		for (int index = 0; index < charray.length; index++) {
			
			if (charray[index] <= '9' && charray[index] >= '0') {
				
				int k = index; //used as an index
				
				StringBuffer strObj = new StringBuffer();
				
				while (k < charray.length&&charray[k] <= '9'&& charray[k]>= '0') 
					strObj.append(charray[k++]);//append link bookmarked
				
				nums.push(Float.parseFloat(strObj.toString()));//converter
				
				index = k- 1;
				} 
			else if (charray[index] == '(')
				ops.push(charray[index]);
			
			else if (charray[index] == ')') {
				
				while (ops.peek() != '(')
					nums.push(compute(ops.pop(), nums.pop(), nums.pop()));
				ops.pop();
				
			} else if (charray[index] == '*' || charray[index] == '+' || charray[index] == '/' || charray[index] == '-') {
				//checks to see if an op
				
				
				while (!ops.isEmpty() && opPriority(charray[index], ops.peek()))
					nums.push(compute(ops.pop(), nums.pop(), nums.pop()));
				ops.push(charray[index]);
			}
		}
		while (!ops.isEmpty())
			nums.push(compute(ops.pop(), nums.pop(), nums.pop()));
		
		return nums.pop();

	}

	private static float compute(char sign, float a, float b) {//used to the the math
		if (sign == '+') {
			return a + b;} 
		else if (sign == '-') {
			return b - a;}
		else if (sign == '*') {
			return b *a;}
		else if (sign == '/') {
			return b/a;}
		else {
			return -1;
		}
	}


}

