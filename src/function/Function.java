package function;

public class Function {
    private final String functionString;
    private ParserNode root;

    /**
     * Constructor.
     * Creates a new function
     * @param function The string representing the function
     */
    public Function(String function) {
        this.functionString = function;
        this.root = Parser.parse(Lexer.lex(function));
        this.removeRendundantOperations(this.root);
    }

    /**
     * Evaluates the function for a certain input of variables
     * @param varNames The array of names of variables
     * @param varValues The array of corresponding variable values
     * @return The result of the computation
     */
    public synchronized double evaluate(String[] varNames, double[] varValues) {
        if (varNames.length != varValues.length) {
            throw new RuntimeException("Variable names and values must have the same number of elements");
        }
        assignValues(root, varNames, varValues);
        return root.getValue();
    }

    /**
     * Assigns the values of variables to the function
     * @param root The node of the parser tree of the function to check
     * @param varNames The array of names of variables
     * @param varValues The array of variable values
     */
    private void assignValues(ParserNode root, String[] varNames, double[] varValues) {
        if (root.getElement().getType() == Token.Type.WORD) {
            boolean found = false;
            // Find the correct variable and set it to the corresponding value
            for (int i=0; i<varNames.length; i++) {
                if (root.getElement().getText().equals(varNames[i])) {
                    root.setReplaceValue(varValues[i]);
                    found = true;
                    break;
                }
            }
            // Set to 0 if the variable value was undefined
            if (!found) {
                root.setReplaceValue(0);
            }
        }
        // Assign value to left child
        if (root.getLeftChild() != null) {
            assignValues(root.getLeftChild(), varNames, varValues);
        }
        // Assign value to right child
        if (root.getRightChild() != null) {
            assignValues(root.getRightChild(), varNames, varValues);
        }
    }

    /**
     * Gets the text of the function.
     * @return The text
     */
    public String getText() {
        return functionString;
    }

    /**                                                     
     * Gets the derivative of this function with respect to a variable
     * @param varName The name of the variable to take the derivative with respect to
     * @return The derivative
     */
    public Function getDerivative(String varName) {
        String df = derive(root, varName);
        return new Function(df);
    }

    /**
     * Takes the derivative at a node, returning a String representation of the derivative function.
     * @param node The node to take the derivative at
     * @param varName The name of the variable to take the derivative with respect to
     * @return The String representation of the derivative
     */
    private String derive(ParserNode node, String varName) {
        boolean hasVar = subTreeHasVar(node, varName),
                leftHasVar = false,
                rightHasVar = false;
        if (hasVar && node.getLeftChild() != null) {
            leftHasVar = subTreeHasVar(node.getLeftChild(), varName);
        }
        if (hasVar && node.getRightChild() != null) {
            rightHasVar = subTreeHasVar(node.getRightChild(), varName);
        }
        String result = "";
        // Different cases
        Token.Type type = node.getElement().getType();
        // Number/constant
        if (type == Token.Type.NUM || type == Token.Type.PI || type == Token.Type.E) {
            result = "0";
        // Variable
        } else if (type == Token.Type.WORD) {
            // Variable taking the derivative with respect to
            if (node.getElement().getText().equals(varName)) {
                result = "1";
            // Other variable
            } else {
                result = "0";
            }
        // Addition
        } else if (type == Token.Type.PLUS) {
            result = "("+derive(node.getLeftChild(), varName)+"+"+derive(node.getRightChild(), varName)+")";
        // Subtraction
        } else if (type == Token.Type.MINUS) {
            result = "("+derive(node.getLeftChild(), varName)+"-"+derive(node.getRightChild(), varName)+")";
        // Multiplication
        } else if (type == Token.Type.MULT) {
            String df = derive(node.getLeftChild(), varName);
            String f = node.getLeftChild().toString();
            String dg = derive(node.getRightChild(), varName);
            String g = node.getRightChild().toString();
            result = "("+df+"*"+g+"+"+f+"*"+dg+")";
        // Division
        } else if (type == Token.Type.DIV) {
            String df = derive(node.getLeftChild(), varName);
            String f = node.getLeftChild().toString();
            String dg = derive(node.getRightChild(), varName);
            String g = node.getRightChild().toString();
            result = "(("+df+"*"+g+"-"+f+"*"+dg+")/"+g+"**2)";
        // Power
        } else if (type == Token.Type.POW) {
            // x**a
            if (leftHasVar && !rightHasVar) {
                String r = node.getRightChild().toString();
                String f = node.getLeftChild().toString();
                String df = derive(node.getLeftChild(), varName);
                result = "("+r+"*"+df+"*"+f+"**"+"("+r+"-1)"+")";
            // a**x
            } else if (!leftHasVar && rightHasVar) {
                String f = node.getRightChild().toString();
                String df = derive(node.getRightChild(), varName);
                String l = node.getLeftChild().toString();
                result = "("+l+"**"+f+"*"+df+"*ln("+l+"))";
            // a**b (both are numbers)
            } else if (!leftHasVar) {
                result = "0";
            }
        // Function
        } else if (type == Token.Type.FUNCTION) {
            String f = node.getLeftChild().toString();
            String df = derive(node.getLeftChild(), varName);
            switch (node.getElement().getText()) {
                case "sin":
                    result = "((" + df + ")*cos(" + f + "))";
                    break;
                case "cos":
                    result = "((" + df + ")*(-sin(" + f + ")))";
                    break;
                case "tan":
                    result = "((" + df + ")*(1+tan(" + f + ")**2))";
                    break;
                case "sqrt":
                    result = "((" + df + ")*0.5/sqrt(" + f + "))";
                    break;
                case "ln":
                    result = "((" + df + ")/(" + f + "))";
                    break;
                case "log":
                    result = "((" + df + ")*log(e)/(" + f + "))";
                    break;
                default:
                    throw new RuntimeException("Unknown derivative formula. '"+node.getElement().getText()+"'");
            }
        }
        return result;
    }

    /**
     * Checks if a parser subtree contains a variable
     * @param root The root of the subtree to check
     * @param varName The name of the variable to check
     * @return {@code true} if it does and {@code false} otherwise
     */
    private boolean subTreeHasVar(ParserNode root, String varName) {
        // If the subtree is a variable and the names match, return true
        if (root.getElement().getType() == Token.Type.WORD) {
            if (root.getElement().getText().equals(varName)) {
                return true;
            }
        }
        // If the left subtree contains the variable, return true
        if (root.getLeftChild() != null && subTreeHasVar(root.getLeftChild(), varName)) {
            return true;
        }
        // If the right subtree contains the variable, return true
        return root.getRightChild() != null && subTreeHasVar(root.getRightChild(), varName);
        // Otherwise, return false
    }

    /**
     * Removes all reduntant operations from the parser tree.
     * Rendundant operations:
     * 0 * n = 0
     * 1 * n = n
     * n * m, where n,m are numbers
     * 0 + n = n
     * n + m, where n,m are numbers
     * n - 0 = n
     * n - m where n, m are numbers
     * n / 1 = n
     * 0 / n = 0 (check if n=0 -> error)
     * n / m where n, m are numbers (check if m=0 -> error)
     * n**0 = 1 (check if n=0 -> error)
     * n**1 = n
     * 0**n = 0 (check if n=0 -> error)
     * 1**n = 1
     * n**m where n,m are numbers (check if n=0 and m=0 || n<0 and m not an integer -> error)
     * @param node The starting node to remove rendundancies from.
     */
    private void removeRendundantOperations(ParserNode node) {
        // Remove redundant operations from left child
        if (node.getLeftChild() != null) {
            removeRendundantOperations(node.getLeftChild());
        }
        // Remove redundant operations from right child
        if (node.getRightChild() != null) {
            removeRendundantOperations(node.getRightChild());
        }
        // Remove redundant operations from node
        // Redundant multiplication
        if (node.getElement().getType() == Token.Type.MULT) {
            // Multiplication with 0
            if (node.getLeftChild().getElement().getText().equals("0") || node.getRightChild().getElement().getText().equals("0")) {
                node.setLeftChild(null);
                node.setRightChild(null);
                node.setElement(new Token(Token.Type.NUM, "0", -1));
            // Multiplication with 1
            } else if (node.getLeftChild().getElement().getText().equals("1")) {
                node.getRightChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getRightChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getRightChild());
                    }
                } else {
                    root = node.getRightChild();
                }
            } else if (node.getRightChild().getElement().getText().equals("1")) {
                node.getLeftChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getLeftChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getLeftChild());
                    }
                } else {
                    root = node.getLeftChild();
                }
            // Both children are numbers
            } else if (node.getLeftChild().getElement().getType() == Token.Type.NUM && node.getRightChild().getElement().getType() == Token.Type.NUM) {
                double left = Double.parseDouble(node.getLeftChild().getElement().getText());
                double right = Double.parseDouble(node.getRightChild().getElement().getText());
                double val = left*right;
                node.setElement(new Token(Token.Type.NUM, val+"", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            }
        // Addition
        } else if (node.getElement().getType() == Token.Type.PLUS) {
            // Adding with 0
            if (node.getLeftChild().getElement().getText().equals("0")) {
                node.getRightChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getRightChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getRightChild());
                    }
                } else {
                    root = node.getRightChild();
                }
            } else if (node.getRightChild().getElement().getText().equals("0")) {
                node.getLeftChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getLeftChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getLeftChild());
                    }
                } else {
                    root = node.getLeftChild();
                }
            // Adding 2 numbers
            } else if (node.getLeftChild().getElement().getType() == Token.Type.NUM && node.getRightChild().getElement().getType() == Token.Type.NUM) {
                double left = Double.parseDouble(node.getLeftChild().getElement().getText());
                double right = Double.parseDouble(node.getRightChild().getElement().getText());
                double val = left + right;
                node.setElement(new Token(Token.Type.NUM, val+"", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            }
        // Subtraction
        } else if (node.getElement().getType() == Token.Type.MINUS) {
            // n-0
            if (node.getRightChild().getElement().getText().equals("0")) {
                node.getLeftChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getLeftChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getLeftChild());
                    }
                } else {
                    root = node.getLeftChild();
                }
            // Both are numbers
            } else if (node.getLeftChild().getElement().getType() == Token.Type.NUM && node.getRightChild().getElement().getType() == Token.Type.NUM) {
                double left = Double.parseDouble(node.getLeftChild().getElement().getText());
                double right = Double.parseDouble(node.getRightChild().getElement().getText());
                double val = left - right;
                node.setElement(new Token(Token.Type.NUM, val+"", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            }
        // Division
        } else if (node.getElement().getType() == Token.Type.DIV) {
            // Division with 1
            if (node.getRightChild().getElement().getText().equals("1")) {
                node.getLeftChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getLeftChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getLeftChild());
                    }
                } else {
                    root = node.getLeftChild();
                }
            // Dividing 0
            } else if (node.getLeftChild().getElement().getText().equals("0")) {
                // Check for division with 0
                if (node.getRightChild().getElement().getText().equals("0")) {
                    throw new RuntimeException("Error. Division with 0.");
                }
                node.setElement(new Token(Token.Type.NUM, "0", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            // Dividing 2 numbers
            } else if (node.getLeftChild().getElement().getType() == Token.Type.NUM && node.getRightChild().getElement().getType() == Token.Type.NUM) {
                double left = Double.parseDouble(node.getLeftChild().getElement().getText());
                double right = Double.parseDouble(node.getRightChild().getElement().getText());
                // Check for division with 0
                if (right == 0) {
                    throw new RuntimeException("Error. Division with 0.");
                }
                double val = left / right;
                node.setElement(new Token(Token.Type.NUM, val+"", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            }
        // Power
        } else if (node.getElement().getType() == Token.Type.POW) {
            // n**0
            if (node.getRightChild().getElement().getText().equals("0")) {
                // Check for 0**0
                if (node.getLeftChild().getElement().getText().equals("0")) {
                    throw new RuntimeException("The operation 0**0 is not allowed");
                }
                node.setElement(new Token(Token.Type.NUM, "1", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            // n**1
            } else if (node.getRightChild().getElement().getText().equals("1")) {
                node.getLeftChild().setParent(node.getParent());
                if (node.getParent() != null) {
                    if (node.getParent().getLeftChild() == node) {
                        node.getParent().setLeftChild(node.getLeftChild());
                    } else if (node.getParent().getRightChild() == node) {
                        node.getParent().setRightChild(node.getLeftChild());
                    }
                } else {
                    root = node.getLeftChild();
                }
            // 0**n
            } else if (node.getLeftChild().getElement().getText().equals("0")) {
                // No need to check for 0**0, it would already have been checked
                node.setElement(new Token(Token.Type.NUM, "0", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            // 1**n
            } else if (node.getLeftChild().getElement().getText().equals("1")) {
                node.setElement(new Token(Token.Type.NUM, "1", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            // number**number
            } else if (node.getLeftChild().getElement().getType() == Token.Type.NUM && node.getRightChild().getElement().getType() == Token.Type.NUM) {
                double left = Double.parseDouble(node.getLeftChild().getElement().getText());
                double right = Double.parseDouble(node.getRightChild().getElement().getText());
                // Check for left being negative and right non-integer
                if (left < 0 && (int) right == right) {
                    throw new RuntimeException("Error. Impossible power operator "+left+"**"+right);
                }
                double val = Math.pow(left, right);
                node.setElement(new Token(Token.Type.NUM, val+"", -1));
                node.setLeftChild(null);
                node.setRightChild(null);
            }
        }
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public String getString(){
        return root.toString();
    }
}
