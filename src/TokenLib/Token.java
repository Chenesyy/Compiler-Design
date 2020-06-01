package TokenLib;

/**
 *
 * @author Ched
 */
public class Token {
    public TokenKind TokenKind;
    
    public Token(TokenKind tokenKind) {
        this.TokenKind = tokenKind;
    }
    
    public TokenKind getTokenType() {
        return TokenKind;
    }
}
