package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenID extends Token {
    public String idName = "";
    
    public TokenID(String name) {
        super(TokenLib.TokenKind.ID);
        idName = name;
    }
}
