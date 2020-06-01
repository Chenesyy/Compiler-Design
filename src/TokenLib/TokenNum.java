package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenNum extends Token {
    public int num = 0;
    
    public TokenNum(int num) {
        super(TokenLib.TokenKind.UNSIGN_INTLIT);
        this.num = num;
    }
}
