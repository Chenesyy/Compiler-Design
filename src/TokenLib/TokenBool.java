package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenBool extends Token {
    public String lit;
    
    public TokenBool(String bool) {
        super(TokenLib.TokenKind.BOOL_CONST);
        lit = bool;
    }
}
