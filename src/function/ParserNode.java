package function;

public class ParserNode {
    private Token element;
    private ParserNode leftChild;
    private ParserNode rightChild;
    private double replaceValue;
    private ParserNode parent;

    /**
     * Constructor. Creates an instance of {@code ParserNode}.
     * 
     * @param element The element contained in the parser node ({@code Token}).
     */
    protected ParserNode(Token element) {
        this.element = element;
        this.leftChild = null;
        this.rightChild = null;
        this.replaceValue = 0;
        this.parent = null;
    }

    /**
     * Constructor. Creates an instance of {@code ParserNode}.
     */
    protected ParserNode() {
        this(null);
    }

    /**
     * Sets the replacing value for a variable of this node
     * 
     * @param val The value to set to
     */
    protected void setReplaceValue(double val) {
        this.replaceValue = val;
    }

    /**
     * Sets the element of this node
     * 
     * @param element The new element to set to
     */
    protected void setElement(Token element) {
        this.element = element;
    }

    /**
     * Sets the left child of this node
     * 
     * @param child The new left child
     */
    protected void setLeftChild(ParserNode child) {
        this.leftChild = child;
    }

    /**
     * Sets the right child of this node
     * 
     * @param child The new right child
     */
    protected void setRightChild(ParserNode child) {
        this.rightChild = child;
    }

    /**
     * Sets the parent of this node
     * 
     * @param parent The new parent
     */
    protected void setParent(ParserNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the element of this node
     * 
     * @return The element or {@code null} if none
     */
    protected Token getElement() {
        return element;
    }

    /**
     * Gets the parent of this node
     * 
     * @return The parent or {@code null} if none
     */
    protected ParserNode getParent() {
        return parent;
    }

    /**
     * Gets the left child of this node
     * 
     * @return The left child or {@code null} if none
     */
    protected ParserNode getLeftChild() {
        return leftChild;
    }

    /**
     * Gets the right child of this node
     * 
     * @return The right child or {@code null} if none
     */
    protected ParserNode getRightChild() {
        return rightChild;
    }

    /**
     * Evaluates this nodes value
     * 
     * @return The value of the node
     */
    protected double getValue() {
        if (element == null) {
            throw new RuntimeException("Null element");
        }
        // Number - return the elements value
        if (element.getType() == Token.Type.NUM) {
            return Double.parseDouble(element.getText());
            // pi - return pi
        } else if (element.getType() == Token.Type.PI) {
            return Math.PI;
            // e - return e
        } else if (element.getType() == Token.Type.E) {
            return Math.E;
            // Plus - return left child + right child
        } else if (element.getType() == Token.Type.PLUS) {
            return leftChild.getValue() + rightChild.getValue();
            // Minus - return left child - right child
        } else if (element.getType() == Token.Type.MINUS) {
            return leftChild.getValue() - rightChild.getValue();
            // Multiplication - return left child * right child
        } else if (element.getType() == Token.Type.MULT) {
            return leftChild.getValue() * rightChild.getValue();
            // Division - return left child / right child
        } else if (element.getType() == Token.Type.DIV) {
            return leftChild.getValue() / rightChild.getValue();
            // Power - return left child ^ right child
        } else if (element.getType() == Token.Type.POW) {
            return Math.pow(leftChild.getValue(), rightChild.getValue());
            // Variable - return the replace value of the node
        } else if (element.getType() == Token.Type.WORD) {
            return replaceValue;
            // Function - return f(left child)
        } else if (element.getType() == Token.Type.FUNCTION) {
            // f = sin
            switch (element.getText()) {
                case "sin":
                    return Math.sin(leftChild.getValue());
                case "cos":
                    return Math.cos(leftChild.getValue());
                case "tan":
                    return Math.tan(leftChild.getValue());
                case "sqrt":
                    return Math.sqrt(leftChild.getValue());
                case "log":
                    return Math.log10(leftChild.getValue());
                case "ln":
                    return Math.log(leftChild.getValue());

                default:
                    throw new RuntimeException(
                            "Unknown function name " + element.getText() + " at position " + element.getPosition());
            }
            // Unknown token type
        } else {
            throw new RuntimeException(
                    "Non-operator/value token type " + element.getText() + " at position " + element.getPosition());
        }
    }

    @Override
    public String toString() {
        boolean hasLeft = getLeftChild() != null;
        boolean hasRight = getRightChild() != null;
        if (hasLeft && hasRight) {
            String s = "";
            if (getParent() != null) {
                if (getElement().getType() == Token.Type.PLUS || getElement().getType() == Token.Type.MINUS) {
                    if (getParent().getElement().getType() == Token.Type.MULT
                            || getParent().getElement().getType() == Token.Type.DIV
                            || getParent().getElement().getType() == Token.Type.POW) {
                        s = "(" + getLeftChild().toString() + element.getText() + getRightChild().toString() + ")";
                    } else {
                        s = getLeftChild().toString() + element.getText() + getRightChild().toString();
                    }
                } else if (getElement().getType() == Token.Type.MULT || getElement().getType() == Token.Type.DIV) {
                    if (getParent().getElement().getType() == Token.Type.POW) {
                        s = "(" + getLeftChild().toString() + element.getText() + getRightChild().toString() + ")";
                    } else {
                        s = getLeftChild().toString() + element.getText() + getRightChild().toString();
                    }
                } else {
                    s = getLeftChild().toString()+element.getText()+getRightChild().toString();
                }
            } else {
                s = getLeftChild().toString() + element.getText() + getRightChild().toString();
            }
            return s;
        } else if (hasLeft) {
            return element.getText() + "(" + getLeftChild().toString() + ")";
        } else {
            return element.getText();
        }
    }
}
