package function;

/**
 * Used for storing tokens of the function
 */
public class Token {
    public static enum Type {
        // Operations
        PLUS, // +
        MINUS, // -
        MULT, // *
        DIV, // /
        POW, // **
        OPEN, // (
        CLOSE, // )
        // Variables
        WORD, // Any word
        FUNCTION, // A function - defined as a word followed by brackets
        // Constants
        E, // e
        PI, // pi
        // Numbers
        NUM // Any number
    }

    private final Type type;
    private String text;
    private final int position;

    /**
     * Constructor. Creates an instance of a {@code Token}.
     * @param type The {@code Type} of the {@code Token}.
     * @param text The text of the {@code Token}.
     * @param position The position of the {@code Token}.
     */
    protected Token(Type type, String text, int position) {
        this.type = type;
        this.text = text;
        this.position = position;
    }

    protected void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the {@code Type} of the {@code Token}.
     * @return The {@code Type}.
     */
    protected Type getType() {
        return type;
    }

    /**
     * Gets the text of the {@code Token}.
     * @return The text
     */
    protected String getText() {
        return text;
    }

    /**
     * Gets the position of the {@code Token}.
     * @return The position
     */
    protected int getPosition() {
        return position;
    }

    @Override 
    public String toString() {
        return "Token([type: "+type.name()+", text: "+text+", position: "+position+"])";
    }
}
