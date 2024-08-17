package function;

import java.util.ArrayList;

/**
 * Used for lexing a function input
 */
public class Lexer {
    /**
     * Lexes a function into a list of {@code Token}s
     * @param function The function to lex (as a {@code String})
     * @return An {@code ArrayList} containing the {@code Token}s
     */
    protected static ArrayList<Token> lex(String function) {
        ArrayList<Token> tokens = new ArrayList<Token>();
        int pos = 0;
        // Loop through the function string and turn it into tokens
        while (pos < function.length()) {
            int tokenStartPos = pos;
            char lookAhead = function.charAt(pos);
            // Space
            if (lookAhead == ' ') {
                pos++;
            } else
            // Plus
            if (lookAhead == '+') {
                pos++;
                tokens.add(new Token(Token.Type.PLUS, lookAhead+"", tokenStartPos));
            } else 
            // Minus
            if (lookAhead == '-') {
                pos++;
                tokens.add(new Token(Token.Type.MINUS, lookAhead+"", tokenStartPos));
            } else
            // Mult and pow
            if (lookAhead == '*') {
                // Mult
                if (pos+1 < function.length() && function.charAt(pos+1) == '*') {
                    pos+=2;
                    tokens.add(new Token(Token.Type.POW, "**", tokenStartPos));
                // Pow
                } else {
                    pos++;
                    tokens.add(new Token(Token.Type.MULT, lookAhead+"", tokenStartPos));
                }
            } else
            // Div
            if (lookAhead == '/') {
                pos++;
                tokens.add(new Token(Token.Type.DIV, lookAhead+"", tokenStartPos));
            } else
            // Number
            if (Character.isDigit(lookAhead)) {
                String text = "";
                boolean dot = false;
                // Keep going while it is a number
                while (pos < function.length() && (Character.isDigit(function.charAt(pos)) || (function.charAt(pos) == '.' && !dot))) {
                    if (function.charAt(pos) == '.') {
                        dot = true;
                    }
                    text += function.charAt(pos);
                    pos++;
                }
                tokens.add(new Token(Token.Type.NUM, text, tokenStartPos));
            } else 
            // Open bracket
            if (lookAhead == '(') {
                pos++;
                tokens.add(new Token(Token.Type.OPEN, lookAhead+"", tokenStartPos));
            } else
            // Closed bracket
            if (lookAhead == ')') {
                pos++;
                tokens.add(new Token(Token.Type.CLOSE, lookAhead+"", tokenStartPos));
            } else
            // Word
            if (Character.isLetter(lookAhead)) {
                // Find the string of the word (only letters and digits)
                String text = "";
                while (pos < function.length() && Character.isLetterOrDigit(function.charAt(pos))) {
                    text += function.charAt(pos);
                    pos++;
                }
                // pi
                if (text.equals("pi")) {
                    tokens.add(new Token(Token.Type.PI, "pi", tokenStartPos));
                // e
                } else if (text.equals("e")) {
                    tokens.add(new Token(Token.Type.E, "e", tokenStartPos));
                // Other
                } else {
                    // Function
                    if (pos < function.length() && function.charAt(pos) == '(') {
                        tokens.add(new Token(Token.Type.FUNCTION, text, tokenStartPos));
                    // Variable
                    } else {
                        tokens.add(new Token(Token.Type.WORD, text, tokenStartPos));
                    }
                }
            } else {
            // Error
                throw new RuntimeException("Unknown character "+lookAhead+" at position "+pos);
            }
        }
        // Insert brackets to prioritize x pow y
        insertBrackets(tokens, new Token.Type[] {Token.Type.POW});
        // Insert brackets to prioritize multiplication and division
        insertBrackets(tokens, new Token.Type[] {Token.Type.MULT, Token.Type.DIV});
        return tokens;
    }

    /**
     * Inserts brackets where needed for multiplication and division
     * @param tokens The list of tokens defining a function
     * @param types The array containing the types you wish to put brackets around
     */
    private static void insertBrackets(ArrayList<Token> tokens, Token.Type[] types) {
        for (int i=0; i<tokens.size(); i++) {
            Token t = tokens.get(i);
            // If the symbol is multiplication or division, insert brackets
            boolean match = false;
            for (Token.Type type : types) {
                if (t.getType() == type) {
                    match = true;
                    break;
                }
            }
            if (match) {
                int openPosition = -1, closePosition = -1;
                // Insert open bracket
                int j=i-1;
                if (j >= 0) {
                    int openBrackets = 0;
                    Token ct = tokens.get(j);
                    do {
                        ct = tokens.get(j);
                        if (ct.getType() == Token.Type.CLOSE) {
                            openBrackets++;
                        } else if (ct.getType() == Token.Type.OPEN) {
                            openBrackets--;
                        }
                        j--;
                    } while (j >= 0 && openBrackets > 0);
                    j++;
                    if (j>0 && tokens.get(j-1).getType() == Token.Type.FUNCTION) {
                        j--;
                    }
                    openPosition = j;
                } else {
                    throw new RuntimeException("Operator "+t.getText()+" cannot be at position 0");
                }
                // Insert closed bracket
                j = i+1;
                if (j < tokens.size()) {
                    int openBrackets = 0;
                    Token ct = tokens.get(j);
                    do {
                        ct = tokens.get(j);
                        if (ct.getType() == Token.Type.FUNCTION) {
                            j++;
                            ct = tokens.get(j);
                        }
                        if (ct.getType() == Token.Type.CLOSE) {
                            openBrackets--;
                        } else if (ct.getType() == Token.Type.OPEN) {
                            openBrackets++;
                        }
                        j++;
                    } while (j < tokens.size() && openBrackets > 0);
                    closePosition = j;
                } else {
                    throw new RuntimeException("Operator "+t.getText()+" cannot be in last position");
                }
                tokens.add(openPosition, new Token(Token.Type.OPEN, "(", -1));
                tokens.add(closePosition+1, new Token(Token.Type.CLOSE, ")", -1));
                i += 1;
            }
        }
    }
}
