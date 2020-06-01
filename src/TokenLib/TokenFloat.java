package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenFloat extends Token {
    public float num = 0;
    
    public TokenFloat(float num) {
        super(TokenLib.TokenKind.FLIT);
        this.num = num;
    }
}
