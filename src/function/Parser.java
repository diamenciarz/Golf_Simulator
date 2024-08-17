package function;

import java.util.ArrayList;

public class Parser {
    /**
     * Parses a list of {@code Token}s into a parser tree.
     * @param tokens The list of {@code Token}s to parse
     */
    protected static ParserNode parse(ArrayList<Token> tokens) {
        ParserNode currentNode = new ParserNode();
        // Loop through all the tokens and turn them into a parser tree
        for (Token t : tokens) {
            // Open bracket
            // Create left child and go there
            if (t.getType() == Token.Type.OPEN) {
                ParserNode newNode = new ParserNode();
                currentNode.setLeftChild(newNode);
                newNode.setParent(currentNode);
                currentNode = newNode;
            // Number/variable
            // Set the current node element to the token and go to the parent
            } else if (t.getType() == Token.Type.NUM || t.getType() == Token.Type.E
                    || t.getType() == Token.Type.PI || t.getType() == Token.Type.WORD) {
                currentNode.setElement(t);
                // If the parent doesn't exist, create a new node and set it to the parent
                // Set the current node to the left child of the new node
                if (currentNode.getParent() == null) {
                    ParserNode newNode = new ParserNode();
                    newNode.setLeftChild(currentNode);
                    currentNode.setParent(newNode);
                    currentNode = newNode;
                } else {
                    currentNode = currentNode.getParent();
                }
            // Closed bracket
            // Go to parent
            } else if (t.getType() == Token.Type.CLOSE) {
                // If the parent doesn't exist, create a new node and set it to the parent
                // Set the current node to the left child of the new node
                if (currentNode.getParent() == null) {
                    ParserNode newNode = new ParserNode();
                    newNode.setLeftChild(currentNode);
                    currentNode.setParent(newNode);
                    currentNode = newNode;
                } else {
                    currentNode = currentNode.getParent();
                }
            // Function
            // Set the current node element to the function token
            } else if (t.getType() == Token.Type.FUNCTION) {
                currentNode.setElement(t);
                ParserNode newNode = new ParserNode();
                currentNode.setLeftChild(newNode);
                newNode.setParent(currentNode);
                currentNode = newNode;
            // Operator
            // Set the first empty node (looking at self and parents) to the operator
            // Create and move to the right child of the node
            } else if (t.getType() == Token.Type.PLUS || t.getType() == Token.Type.MINUS 
                    || t.getType() == Token.Type.MULT || t.getType() == Token.Type.DIV
                    || t.getType() == Token.Type.POW) {
                // Proceed going to parent until an empty node is found
                while (currentNode.getElement() != null) {
                    // If the parent doesn't exist, create a new node and set it to the parent
                    // Set the current node to the left child of the new node
                    if (currentNode.getParent() == null) {
                        ParserNode newNode = new ParserNode();
                        newNode.setLeftChild(currentNode);
                        currentNode.setParent(newNode);
                        currentNode = newNode;
                    } else {
                        currentNode = currentNode.getParent();
                    }
                }
                // Check if the expression is a -n expression and turn it into 0-n
                if (t.getType() == Token.Type.MINUS && currentNode.getLeftChild() == null) {
                    ParserNode left = new ParserNode(new Token(Token.Type.NUM, "0", -1));
                    currentNode.setLeftChild(left);
                    left.setParent(currentNode);
                }
                currentNode.setElement(t);
                ParserNode newNode = new ParserNode();
                newNode.setParent(currentNode);
                currentNode.setRightChild(newNode);
                currentNode = newNode;
            }
        }
        // Find the root node
        while (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }
        // Remove empty nodes
        removeEmptyNodes(currentNode);
        // Find the root again and return it
        while (currentNode.getElement() == null) {
            currentNode = currentNode.getLeftChild();
        }
        while (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }
        return currentNode;
    }

    /**
     * Removes all the empty nodes from a parser tree
     * @param root The root of the parser tree
     */
    private static void removeEmptyNodes(ParserNode root) {
        // Remove the node if empty
        if (root.getElement() == null) {
            // If it has a parent, connect it to children
            if (root.getParent() != null) {
                // Check if the current node's parent has the node as left or right child
                if (root.getParent().getLeftChild() == root) {
                    if (root.getLeftChild() != null) {
                        root.getParent().setLeftChild(root.getLeftChild());
                        root.getLeftChild().setParent(root.getParent());
                    } else if (root.getRightChild() != null) {
                        root.getParent().setLeftChild(root.getRightChild());
                        root.getRightChild().setParent(root.getParent());
                    } else {
                        root.getParent().setLeftChild(null);
                    }
                } else if (root.getParent().getRightChild() == root) {
                    if (root.getLeftChild() != null) {
                        root.getParent().setRightChild(root.getLeftChild());
                        root.getLeftChild().setParent(root.getParent());
                    } else if (root.getRightChild() != null) {
                        root.getParent().setRightChild(root.getRightChild());
                        root.getRightChild().setParent(root.getParent());
                    } else {
                        root.getParent().setRightChild(null);
                    }
                }
            // Otherwise, just remove left childs parent
            } else {
                if (root.getLeftChild() != null) {
                    root.getLeftChild().setParent(null);
                }
            }
        }
        // Remove left child empty node
        if (root.getLeftChild() != null) {
            removeEmptyNodes(root.getLeftChild());
        }
        // Remove right child empty node
        if (root.getRightChild() != null) {
            removeEmptyNodes(root.getRightChild());
        }
    }
}
