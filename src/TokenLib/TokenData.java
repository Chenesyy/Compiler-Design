package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenData extends Token {
    public String dataType;
    
    public TokenData(String dataType) {
        super(TokenLib.TokenKind.DATATYPE);
        this.dataType = dataType;
    }
}
