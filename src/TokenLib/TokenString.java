package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenString extends Token {
    public String literal;
    
    public TokenString(String name) {
        super(TokenLib.TokenKind.STRLIT);
        literal = name;
    }
}
