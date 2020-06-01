package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenChar extends Token {
    public char literal = 0;
    
    public TokenChar(char name) {
        super(TokenLib.TokenKind.CHARLIT);
        literal = name;
    }
}
