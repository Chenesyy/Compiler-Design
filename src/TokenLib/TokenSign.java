package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenSign extends Token {

    public int num = 0;

    public TokenSign(int num) {
        super(TokenLib.TokenKind.SIGN_INTLIT);
        this.num = num;
    }

}
