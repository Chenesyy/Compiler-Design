package TokenLib;

/**
 *
 * @author Ched
 */
public class TokenAttribute {

    private int intVal;
    private float floatVal;
    private char charVal;
    private boolean boolVal;
    private String idVal;
    private String strVal;

    public TokenAttribute() {
    }

    public TokenAttribute(int intVal) {
        this.intVal = intVal;
    }

    public TokenAttribute(float floatVal) {
        this.floatVal = floatVal;
    }

    public TokenAttribute(char charVal) {
        this.charVal = charVal;
    }

    public TokenAttribute(boolean boolVal) {
        this.boolVal = boolVal;
    }

    public TokenAttribute(String idVal) {
        this.idVal = idVal;
        this.strVal = idVal;
    }

    public String getStrVal() {
        return strVal;
    }

    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }

    public char getCharVal() {
        return charVal;
    }

    public void setCharVal(char charVal) {
        this.charVal = charVal;
    }

    public boolean getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
    }

    public String getIdVal() {
        return idVal;
    }

    public void setIdVal(String idVal) {
        this.idVal = idVal;
    }
}
