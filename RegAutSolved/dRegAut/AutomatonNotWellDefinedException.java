package dRegAut;

/**
 * Exception caused by an automaton not being well-defined. 
 * @see FA#checkWellDefined()
 * @see NFA#checkWellDefined()
 * @see NFALambda#checkWellDefined()
 */
public class AutomatonNotWellDefinedException extends RuntimeException {
    
    /** 
     * Constructs a new <tt>AutomatonNotWellDefinedException</tt>. 
     * @param msg error description
     */
    public AutomatonNotWellDefinedException(String msg) {
        super(msg);
    }
}
