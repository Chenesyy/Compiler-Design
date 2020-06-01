package compilerdesign;

import TokenLib.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author Ched
 */
public class Lexical { //Nagloloko si ID pag may ID sa huli

    public static PushbackInputStream bitSyntax;
    public static PushbackInputStream bitSyntax2;
    public static PushbackInputStream bitSyntax3;
    HashMap<String, TokenKind> rwTbl;
    HashMap<String, TokenKind> opTbl;
    HashMap<String, ArrayList<Object>> idTbl;
//    HashMap<String, TokenID> idTbl;
    byte[] farray = new byte[1];
    byte[] array;
    byte[] larray;
    byte[] buntot;
    int count = 0;
    char l; //Look
    char t; //Tail
    int look = 0;
    int tail = 0;
    int currentBit = 0;
    public String errMsg;
    static int line = 1;

    public Lexical(HashMap<String, TokenKind> rwTbl, HashMap<String, ArrayList<Object>> idTbl, HashMap<String, TokenKind> opTbl, String fileName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        this.rwTbl = rwTbl;
        this.idTbl = idTbl;
        this.opTbl = opTbl;

        try {
            bitSyntax = ReadFile.ReadText(fileName);

            array = Files.readAllBytes(Paths.get(fileName));
            array = Arrays.copyOfRange(array, 1, array.length);
            ByteArrayInputStream arr = new ByteArrayInputStream(array);
            PushbackInputStream push = new PushbackInputStream(arr);
            bitSyntax2 = push;

            farray[0] = 67;
            larray = Files.readAllBytes(Paths.get(fileName));
            output.write(farray);
            output.write(larray);
            buntot = output.toByteArray();
            ByteArrayInputStream arr2 = new ByteArrayInputStream(buntot);
            PushbackInputStream tulak = new PushbackInputStream(arr2);
            bitSyntax3 = tulak;
        } catch (Exception e) {
            System.out.println(e);
//            System.out.println("Error in source file");
            errMsg = "Error in source file";
        }
    }

    static boolean IsInputEnd() {
        try {
            int i = bitSyntax.read();

            if (i != -1) {
                bitSyntax.unread(i);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public Token getToken() {
        //Token reqToken;
        try {
            int CURRENT_STATE = 0;
            boolean returnString = false;
            String tokenS = "";
            String sym = "+-*/~><#%^(){};'=!&|.,@";

            while (!returnString) {
                char c = Consume();
                l = Eat();                                               //WORKING ON IT
                t = Kain();
                tokenS += c;

                switch (CURRENT_STATE) {
                    case 0:
                        if (count == 1) {
                            count = 0;
                            CURRENT_STATE = 6;
                        } else if (count == 2) {
                            count = 0;
                            CURRENT_STATE = 94;
                        } else if (currentBit == 10) {
                            return new Token(TokenKind.NEWLINE);
                        } else if (c == '$') {
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.WHITESPACE); //CHECKS WHITESPACE 
                        } else if (c == ' ' || currentBit == 9) {//single quote. CHECKS SYMBOL
                            CURRENT_STATE = 7;
                        } else if (c == '"') {
                            if (l == '\\') {
                                return new Token(TokenKind.DLEFT);
                            } else {
                                tokenS = Remove(tokenS);//not sure if require ksi gumagana kahit wala
                                CURRENT_STATE = 5;
                            }
                        } else if (c == '\\') {//dobol and single quote and \n
                            CURRENT_STATE = 93;
                        } else if (currentBit == 39) {
                            if (l == '\\') {
                                return new Token(TokenKind.SLEFT);
                            } else {
                                tokenS = Remove(tokenS);
                                CURRENT_STATE = 92;
                            }
                        } else if (c == '{') {
                            return new Token(TokenKind.LCURL);
                        } else if (c == '}') {
                            return new Token(TokenKind.RCURL);
                        } else if (c == ';') {
                            return new Token(TokenKind.SMICOL);
                        } else if (c == ',') {
                            return new Token(TokenKind.COMMA);
                        } else if (c == '(') {
                            return new Token(TokenKind.LPAREN);
                        } else if (c == ')') {
                            return new Token(TokenKind.RPAREN);
                        } else if (c == '~') {//single comment
                            CURRENT_STATE = 95;
                        } else if (c == '#') {//multicomment
                            CURRENT_STATE = 97;
                        } else if (c == '&') {//CHECKS LOGOP
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.LOGOP);
                            }
                            return new Token(TokenKind.LOGOP);
                        } else if (c == '|') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.LOGOP);
                            }
                            return new Token(TokenKind.LOGOP);
                        } else if (c == '!') {//CHECKS LOGOP AND RELOP
                            CURRENT_STATE = 31;
                        } else if (c == '<') {//RELOP. 
                            CURRENT_STATE = 30;
                        } else if (c == '>') {//RELOP. 
                            CURRENT_STATE = 102;
                        } else if (c == '=') {//RELOP AND ASSIGN.
                            CURRENT_STATE = 32;
                        } else if (c == '@') {//CHECKS MATCH
                            CURRENT_STATE = 101;
                        } else if (c == '+') {//CHECKS ADDSUB AND UNOP
                            if ((AlphaOrDigit(t) || t == ')') && !(l == '+') || ((t == ' ') && (l == ' '))) {
                                return new Token(TokenKind.ADDSUB);
                            } else if (l == '+' && /*(t == ' ' || t == '=' || tail == 9)*/ !(AlphaOrDigit(t) || t == ')')) {
                                CURRENT_STATE = 121;
                            } else if (l == '+' && (AlphaOrDigit(t) || t == ')')) {
                                CURRENT_STATE = 120;
                            } else if (IsDigit(l)) {
                                CURRENT_STATE = 33;
                            } else {
                                //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                                errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                                return new Token(TokenKind.ERROR);
                            }
                        } else if (c == '-') {
                            if ((AlphaOrDigit(t) || t == ')') && !(l == '-') || ((t == ' ') && (l == ' '))) {//var-65
                                return new Token(TokenKind.ADDSUB);
                            } else if (l == '-' && /*(t == ' ' || t == '=' || tail == 9)*/ !(AlphaOrDigit(t) || t == ')')) {//--d
                                CURRENT_STATE = 122;
                            } else if (l == '-' && (AlphaOrDigit(t) || t == ')')) {//7-----5
                                CURRENT_STATE = 123;
                            } else if (IsDigit(l)) {//-7
                                CURRENT_STATE = 33;
                            } else {
                                //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                                errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                                return new Token(TokenKind.ERROR);
                            }
                        } else if (c == '*' || c == '/') {//CHECKS MULDIV
                            return new Token(TokenKind.MULDIV);
                        } else if (c == '^') {//CHECKS EXPO
                            return new Token(TokenKind.EXPO);
                        } else if (c == 'S') {//SEED case1-4
                            CURRENT_STATE = 1;
                        } else if (c == 'H') {//HARVEST
                            CURRENT_STATE = 9;
                        } else if (c == 'u') {//uproot
                            CURRENT_STATE = 16;
                        } else if (c == 'g') {//grow
                            CURRENT_STATE = 37;
                        } else if (c == 'T') {//TRUE
                            CURRENT_STATE = 22;
                        } else if (c == 'F') {//FALSE
                            CURRENT_STATE = 26;
                        } else if (c == 'i') {//IF&INT
                            CURRENT_STATE = 43;
                        } else if (c == 'e') {//ELSE&ELIF
                            CURRENT_STATE = 47;
                        } else if (c == 'w') {//while
                            CURRENT_STATE = 53;
                        } else if (c == 'f') {//float
                            CURRENT_STATE = 58;
                        } else if (c == 's') {//strbean&sprout
                            CURRENT_STATE = 63;
                        } else if (c == 'm') {//mungbean
                            CURRENT_STATE = 70;
                        } else if (c == 'b') {//bool
                            CURRENT_STATE = 78;
                        } else if (c == 'p') {//plant
                            CURRENT_STATE = 82;
                        } else if (c == 't') {//toString toInt
                            CURRENT_STATE = 103;
                        } else if (c == 'l') {
                            CURRENT_STATE = 114;
                        } else if (IsAlphabet(c)) {//ID
                            CURRENT_STATE = 8;
                        } else if (IsDigit(c)) {
                            CURRENT_STATE = 41;
                        } else {
                            if (currentBit != 13 && currentBit != 255) {
                                //System.out.println("[ERROR: '" + c + "'" + " CANNOT BE RECOGNIZED]");
                                errMsg = "[ERROR: '" + c + "'" + " CANNOT BE RECOGNIZED]";
                                return new Token(TokenKind.ERROR);
                            } else {
                                line++;
                            }
                        }
                        break;

                    case 1:
                        if (c == 'E') {//SEED
                            CURRENT_STATE = 2;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 2:
                        if (c == 'E') {//SEED
                            CURRENT_STATE = 3;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 3:
                        if (c == 'D') {//SEED
                            CURRENT_STATE = 4;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                               //idTbl.put(tokenS, new TokenID(tokenS));
                               idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 4://case 6 nya
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 5://case 95&96 nya. For DobolQuote
                        if (AlphaOrDigit(c) || c == ' ' || sym.indexOf(c) != -1 || sym.indexOf(c) == -1) {
                            Push();
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.DLEFT);
                        } else {
                            return new Token(TokenKind.ERROR);
                        }

                    case 6://case 106. STRLIT
                        if (c != '\\' && c != ';') {
                            CURRENT_STATE = 6;
                        } else if (c == ';') {
                            //System.out.print("[ERROR: MISSING MATCHING RIGHT QUOTE] ");
                            errMsg = "[ERROR: MISSING MATCHING RIGHT QUOTE] ";
                            return new Token(TokenKind.ERROR);
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new TokenString(tokenS);
                        }
                        break;

                    case 7://Whitespace
                        if (c == ' ') {
                            CURRENT_STATE = 7;
                        } else {
                            Pushback();
                            return new Token(TokenKind.WHITESPACE);
                        }
                        break;

                    case 8://ID
                        if (AlphaOrDigit(c) /*|| currentBit == 58 || c == '-' || c == ':' || sym.indexOf(c) != -1*/) {
                            CURRENT_STATE = 8;
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        }
                        break;

                    case 9://HARVEST case 10
                        if (c == 'A') {
                            CURRENT_STATE = 10;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS.trim()));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 10://HARVEST
                        if (c == 'R') {
                            CURRENT_STATE = 11;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 11://HARVEST
                        if (c == 'V') {
                            CURRENT_STATE = 12;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 12://HARVEST
                        if (c == 'E') {
                            CURRENT_STATE = 13;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 13://HARVEST
                        if (c == 'S') {
                            CURRENT_STATE = 14;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 14://HARVEST
                        if (c == 'T') {
                            CURRENT_STATE = 15;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 15://case 14. HARVEST
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 16://uproot
                        if (c == 'p') {
                            CURRENT_STATE = 17;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 17://uproot
                        if (c == 'r') {
                            CURRENT_STATE = 18;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 18://uproot
                        if (c == 'o') {
                            CURRENT_STATE = 19;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 19://uproot
                        if (c == 'o') {
                            CURRENT_STATE = 20;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 20://uproot
                        if (c == 't') {
                            CURRENT_STATE = 21;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 21://uproot
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 22://TRUE
                        if (c == 'R') {
                            CURRENT_STATE = 23;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 23://TRUE
                        if (c == 'U') {
                            CURRENT_STATE = 24;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 24://TRUE
                        if (c == 'E') {
                            CURRENT_STATE = 25;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 25://BOOL_CONST. case 111&116
                        if (!(IsAlphabet(c) || IsDigit(c))) {
//                        Pushback();
//                        tokenS = Remove(tokenS);
//                        return new Token(TokenKind.BOOL_CONST);
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new TokenBool(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 26://FALSE
                        if (c == 'A') {
                            CURRENT_STATE = 27;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 27://FALSE
                        if (c == 'L') {
                            CURRENT_STATE = 28;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 28://FALSE
                        if (c == 'S') {
                            CURRENT_STATE = 29;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 29://FALSE
                        if (c == 'E') {
                            CURRENT_STATE = 25;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 30://RELOP&INPUT <
                        if (c == ' ' || IsDigit(c) || IsAlphabet(c) || c == '+' || currentBit == 9) { //pinalitan after check, + and currbit = 9
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);
                        } else if (c == '-') {
                            if (IsDigit(l)) {
                                Pushback();
                                //Puke();
                                tokenS = Remove(tokenS);
                                if (!opTbl.containsKey(tokenS.trim())) {
                                    opTbl.put(tokenS.trim(), TokenKind.RELOP);
                                }
                                return new Token(TokenKind.RELOP);
                            } else {
                                return new Token(TokenKind.INPUT);
                            }
                        } else if (c == '=') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);
                        } else {
                            //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 31://RELOP&LOGOP INCOMPLETE
                        if (IsAlphabet(c)) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.LOGOP);
                            }
                            return new Token(TokenKind.LOGOP);
                        } else if (c == '=') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);
                        } else if (c == '@') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.MATCH);
                            }
                            return new Token(TokenKind.MATCH);
                        } else {
//                            System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 32://RELOP&ASIGN =
                        if (c == '=') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);
                        } else if (c == ' ' || AlphaOrDigit(c) || c == '"' || c == '-' || currentBit == 39 || c == '+' || currentBit == 9 || c == '~' || c == '#') { //Palit after check, dinagdagan ng " ' 39 and ~ , #
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.ASSIGN);
                        } else {
                            //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED = ERROR]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 33://ADDSUB    
                        if (IsDigit(c)) {
                            CURRENT_STATE = 100;
                        } else {
                            //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 34://UNOP
                        if (IsAlphabet(c)) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.UNOP);
                            }
                            return new Token(TokenKind.UNOP);
                        } else {
                            //System.out.println("[ERROR: '" + c + "' MUST BE A VARIABLE]");
                            errMsg = "[ERROR: '" + c + "' MUST BE A VARIABLE]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 37://grow
                        if (c == 'r') {
                            CURRENT_STATE = 38;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 38://grow
                        if (c == 'o') {
                            CURRENT_STATE = 39;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 39://grow
                        if (c == 'w') {
                            CURRENT_STATE = 40;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 40://grow
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 41://case 105. INTLIT //CHANGE AFTER CHECK, DINAGDAGAN NG currentbit == 9. Nagdagdag si OG lexical ng currentBit == 10 kaso wala parin
                        if (IsDigit(c)) {
                            CURRENT_STATE = 41;
                        } else if (c == '.') {
                            CURRENT_STATE = 42;
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS);
//                        if (!numeric.containsKey(Integer.parseInt(tokenS))) {
//                            numeric.put(Integer.parseInt(tokenS), new TokenNum(Integer.parseInt(tokenS)));
//                        }
                            return new TokenNum(Integer.parseInt(tokenS));
                        }
                        break;

                    case 42://118. FLIT
                        if (IsDigit(c)) {
                            CURRENT_STATE = 42;
                        } else if (c == ' ' || c == ';' || c == ')' || c == ',' || c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '<' || c == '>' || c == '!' || c == '=' || c == '~' || c == '#' || currentBit == 10 || currentBit == 13 || currentBit == 9) {
                            Pushback();
                            tokenS = Remove(tokenS);
//                        if (!floats.containsKey(Float.parseFloat(tokenS))) {
//                            floats.put(Float.parseFloat(tokenS), new TokenFloat(Float.parseFloat(tokenS)));
//                        }
                            return new TokenFloat(Float.parseFloat(tokenS));
                        } else {
                            //System.out.println("[ERROR: '" + c + "' IS NOT A NUMBER NOR A RELATIONAL OPERATOR]");
                            errMsg = "[ERROR: '" + c + "' IS NOT A NUMBER NOR A RELATIONAL OPERATOR]";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 43://IF and INT
                        if (c == 'f') {
                            CURRENT_STATE = 44;
                        } else if (c == 'n') {
                            CURRENT_STATE = 45;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 44://IF
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 45://INT
                        if (c == 't') {
                            CURRENT_STATE = 46;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 46://INT
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 47://ELSE&ELIF
                        if (c == 'l') {
                            CURRENT_STATE = 48;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 48://ELSE
                        if (c == 's') {
                            CURRENT_STATE = 49;
                        } else if (c == 'i') {
                            CURRENT_STATE = 51;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 49://ELSE
                        if (c == 'e') {
                            CURRENT_STATE = 50;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 50://ELSE
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 51://ELIF
                        if (c == 'f') {
                            CURRENT_STATE = 52;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 52://ELIF
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 53://while
                        if (c == 'h') {
                            CURRENT_STATE = 54;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 54://while
                        if (c == 'i') {
                            CURRENT_STATE = 55;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 55://while
                        if (c == 'l') {
                            CURRENT_STATE = 56;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 56://while
                        if (c == 'e') {
                            CURRENT_STATE = 57;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 57://while
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 58://float
                        if (c == 'l') {
                            CURRENT_STATE = 59;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS.trim()));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 59://float
                        if (c == 'o') {
                            CURRENT_STATE = 60;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 60://float
                        if (c == 'a') {
                            CURRENT_STATE = 61;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 61://float
                        if (c == 't') {
                            CURRENT_STATE = 62;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 62://float
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 63://strbean&sprout
                        if (c == 't') {
                            CURRENT_STATE = 64;
                        } else if (c == 'p') {
                            CURRENT_STATE = 87;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 64://strbean
                        if (c == 'r') {
                            CURRENT_STATE = 65;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 65://strbean
                        if (c == 'b') {
                            CURRENT_STATE = 66;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 66://strbean
                        if (c == 'e') {
                            CURRENT_STATE = 67;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 67://strbean
                        if (c == 'a') {
                            CURRENT_STATE = 68;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 68://strbean
                        if (c == 'n') {
                            CURRENT_STATE = 69;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 69://strbean
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 70://mungbean
                        if (c == 'u') {
                            CURRENT_STATE = 71;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 71://mungbean
                        if (c == 'n') {
                            CURRENT_STATE = 72;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 72://mungbean
                        if (c == 'g') {
                            CURRENT_STATE = 73;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 73://mungbean
                        if (c == 'b') {
                            CURRENT_STATE = 74;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 74://mungbean    
                        if (c == 'e') {
                            CURRENT_STATE = 75;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 75://mungbean
                        if (c == 'a') {
                            CURRENT_STATE = 76;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 76://mungbean
                        if (c == 'n') {
                            CURRENT_STATE = 77;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 77://mungbean
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 78://bool
                        if (c == 'o') {
                            CURRENT_STATE = 79;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 79://bool
                        if (c == 'o') {
                            CURRENT_STATE = 80;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 80://bool
                        if (c == 'l') {
                            CURRENT_STATE = 81;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 81://bool
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 82://plant
                        if (c == 'l') {
                            CURRENT_STATE = 83;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 83://plant
                        if (c == 'a') {
                            CURRENT_STATE = 84;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 84://plant
                        if (c == 'n') {
                            CURRENT_STATE = 85;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 85://plant
                        if (c == 't') {
                            CURRENT_STATE = 86;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 86://plant
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 87://sprout
                        if (c == 'r') {
                            CURRENT_STATE = 88;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 88://sprout
                        if (c == 'o') {
                            CURRENT_STATE = 89;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 89://sprout
                        if (c == 'u') {
                            CURRENT_STATE = 90;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 90://sprout
                        if (c == 't') {
                            CURRENT_STATE = 91;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 91://sprout
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 92://SLEFT
                        if (AlphaOrDigit(c) || c == ' ' || sym.indexOf(c) != -1 || sym.indexOf(c) == -1) {
                            PushB();
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.SLEFT);
                        } else {
                            return new Token(TokenKind.ERROR);
                        }

                    case 93://right dquote
                        if (c == '"') {
                            return new Token(TokenKind.DRIGHT);
                        } else if (currentBit == 39) {
                            return new Token(TokenKind.SRIGHT);
                        } else if (c == 'n' && l == '\\') {
                            return new Token(TokenKind.IGNORE);
                        } else if (c == 'n') {
                            count = 1;
                            return new Token(TokenKind.IGNORE);
                        } else if (c == '#') {
                            CURRENT_STATE = 97;
                        } else {
                            //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 94:
                        if (c == '\\') {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new TokenChar(tokenS.charAt(0));
                        } else {
                            //System.out.println("[ERROR: EXPECTED 1 CHARACTER]");
                            errMsg = "[ERROR: EXPECTED 1 CHARACTER]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 95://single comment
                        if (c == '~') {
                            CURRENT_STATE = 96;
                        } else if (c == '"' || currentBit == 39 || AlphaOrDigit(c) || c == ' ' || c == '+' || c == '-' || c == '#') {
                            Pushback();
                            return new Token(TokenKind.CONCAT);
                        } else {
                            //System.out.println("[ERROR: UNEXPECTED SYMBOL '" + c + "']");
                            errMsg = "[ERROR: UNEXPECTED SYMBOL '" + c + "']";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 96://single comment
                        if (currentBit == 10) {
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.COMMENT);
                        } else {
                            CURRENT_STATE = 96;
                        }
                        break;

                    case 97://multiline comment
                        if (c == '~') {
                            CURRENT_STATE = 98;
                        } else {
                            //System.out.println("[ERROR: '~' EXPECTED]");
                            errMsg = "[ERROR: '~' EXPECTED]";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 98://multiline
                        if (c != '~') {
                            CURRENT_STATE = 98;
                        } else {
                            CURRENT_STATE = 99;
                        }
                        break;

                    case 99://multiline
                        if (c != '#') {
                            CURRENT_STATE = 98;
                        } else if (c == '#') {
                            tokenS = Remove(tokenS);
                            return new Token(TokenKind.COMMENT);
                        } else {
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 100://SIGNED INT
                        if (IsDigit(c)) {
                            CURRENT_STATE = 100;
                        } else if (c == '.') { //<- CHANGE AFTER CHECK, para hnd nababasa si -34.3 or +34.5
                            //System.out.println("[ERROR: TYPE FLOAT CANNOT START WITH + OR -]");
                            errMsg = "[ERROR: TYPE FLOAT CANNOT START WITH + OR -]";
                            return new Token(TokenKind.ERROR);
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS);
//                        if (!sign_numeric.containsKey(Integer.parseInt(tokenS))) {
//                            sign_numeric.put(Integer.parseInt(tokenS), new TokenSign(Integer.parseInt(tokenS)));
//                        }
                            return new TokenSign(Integer.parseInt(tokenS));
                        }
                        break;

                    case 101://MATCH @=
                        if (c == '=') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.MATCH);
                            }
                            return new Token(TokenKind.MATCH);
                        } else {
                            //System.out.println("[ERROR: '=' EXPECTED]");
                            errMsg = "[ERROR: '=' EXPECTED]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 102://RELOP for >=
                        if (c == '=') {
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);//>=
                        } else if (c == ' ' || AlphaOrDigit(c) || c == '+' || c == '-' || currentBit == 9) {//palit after check, + -
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!opTbl.containsKey(tokenS.trim())) {
                                opTbl.put(tokenS.trim(), TokenKind.RELOP);
                            }
                            return new Token(TokenKind.RELOP);//>
                        } else {
                            //System.out.println("[ERROR: '" + c + "' CANNOT BE RECOGNIZED]");
                            errMsg = "[ERROR: '" + c + "' CANNOT BE RECOGNIZED]";
                            return new Token(TokenKind.ERROR);
                        }

                    case 103://toString toInt
                        if (c == 'o') {
                            CURRENT_STATE = 104;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 104://toString toInt
                        if (c == 'S') {
                            CURRENT_STATE = 105;
                        } else if (c == 'I') {
                            CURRENT_STATE = 111;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 105://toString
                        if (c == 't') {
                            CURRENT_STATE = 106;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 106://toString
                        if (c == 'r') {
                            CURRENT_STATE = 107;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 107://toString
                        if (c == 'i') {
                            CURRENT_STATE = 108;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 108://toString
                        if (c == 'n') {
                            CURRENT_STATE = 109;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 109://toString
                        if (c == 'g') {
                            CURRENT_STATE = 110;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 110://toString
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 111://toInt
                        if (c == 'n') {
                            CURRENT_STATE = 112;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 112://toInt
                        if (c == 't') {
                            CURRENT_STATE = 113;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 113://length
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 114://length
                        if (c == 'e') {
                            CURRENT_STATE = 115;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 115://length
                        if (c == 'n') {
                            CURRENT_STATE = 116;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 116://length
                        if (c == 'g') {
                            CURRENT_STATE = 117;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 117://length
                        if (c == 't') {
                            CURRENT_STATE = 118;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 118://length
                        if (c == 'h') {
                            CURRENT_STATE = 119;
                        } else if (sym.indexOf(c) != -1) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            if (!idTbl.containsKey(tokenS.trim())) {
                                //idTbl.put(tokenS, new TokenID(tokenS));
                                idTbl.computeIfAbsent(tokenS.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(new TokenID(tokenS.trim())));
                            }
                            return new TokenID(tokenS);
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 119://length
                        if (!(IsAlphabet(c) || IsDigit(c))) {
                            Pushback();
                            tokenS = Remove(tokenS);
                            return new Token(rwTbl.get(tokenS));
                        } else {
                            CURRENT_STATE = 8;
                        }
                        break;

                    case 120://5+++++++6
                        if (c == '+' && !IsDigit(l)) {
                            CURRENT_STATE = 120;
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS); //NOT SURE
                            return new Token(TokenKind.ADDSUB);
                        }
                        break;

                    case 121://UNOP
                        if (c == '+' /*&& IsAlphabet(l)*/) {
                            CURRENT_STATE = 34;
                        } else {
                            //System.out.print("[ERROR: '+' EXPECTED] ");
                            errMsg = "[ERROR: '+' EXPECTED] ";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 122:
                        if (c == '-') {
                            CURRENT_STATE = 34;
                        } else {
                            //System.out.print("[ERROR: '-' EXPECTED] ");
                            errMsg = "[ERROR: '-' EXPECTED] ";
                            return new Token(TokenKind.ERROR);
                        }
                        break;

                    case 123://6------7
                        if (c == '-' && !IsDigit(l)) {
                            CURRENT_STATE = 123;
                        } else {
                            Pushback();
                            tokenS = Remove(tokenS); //NOT SURE
                            return new Token(TokenKind.ADDSUB);
                        }
                        break;
                }

            }
        } catch (Exception e) {
            //System.out.println("\n[ERROR: " + e.getMessage() + "]");
            errMsg = "[ERROR: " + e.getMessage() + "]";
            return new Token(TokenKind.ERROR);
        }
        return new Token(TokenKind.ERROR);
    }

    char Consume() {
        try {
            currentBit = bitSyntax.read();
            if (currentBit == -1) {
                return '$'; //pinalitan after check, from ' ' to '$'
            }
            return (char) currentBit;

        } catch (Exception e) {
            //System.out.println("CONSUME METHOD ERROR");
            errMsg = "CONSUME METHOD ERROR";
        }
        return '0';
    }

    char Eat() {
        try {
            look = bitSyntax2.read();
            if (look == -1) {
                return ' ';
            }
            return (char) look;

        } catch (Exception e) {
            //System.out.println("CONSUME METHOD ERROR");
            errMsg = "CONSUME METHOD ERROR";
        }
        return '0';
    }

    char Kain() {
        try {
            tail = bitSyntax3.read();
            if (tail == -1) {
                return ' ';
            }
            return (char) tail;

        } catch (Exception e) {
            //System.out.println("CONSUME METHOD ERROR");
            errMsg = "CONSUME METHOD ERROR";
        }
        return '0';
    }

    void Pushback() {
        try {
            bitSyntax.unread(currentBit);
            bitSyntax2.unread(look);
            bitSyntax3.unread(tail);
        } catch (Exception e) {
            //System.out.println("PUSHBACK METHOD ERROR");
            errMsg = "PUSHBACK METHOD ERROR";
        }
    }

    void Push() {
        try {
            bitSyntax.unread(currentBit);
            bitSyntax2.unread(look);
            bitSyntax3.unread(tail);
            count = 1;
        } catch (Exception e) {
            //System.out.println("PUSHBACK METHOD ERROR");
            errMsg = "PUSHBACK METHOD ERROR";
        }
    }

    void PushB() {
        try {
            bitSyntax.unread(currentBit);
            bitSyntax2.unread(look);
            bitSyntax3.unread(tail);
            count = 2;
        } catch (Exception e) {
            //System.out.println("PUSHBACK METHOD ERROR");
            errMsg = "PUSHBACK METHOD ERROR";
        }
    }

    String Remove(String tokenString) {
        return tokenString.substring(0, tokenString.length() - 1);
    }

    boolean IsAlphabet(char c) {
        return Character.isLetter(c);
    }

    boolean IsDigit(char c) {
        return Character.isDigit(c);
    }

    boolean AlphaOrDigit(char c) {
        return (Character.isDigit(c) || Character.isLetter(c));
    }

    boolean AlphaAndDigit(char c) {
        return (Character.isDigit(c) && Character.isLetter(c));
    }

}
