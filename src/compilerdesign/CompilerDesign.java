package compilerdesign;

import TokenLib.*;
import java.io.IOException;
import java.util.HashMap;
import grtree.NPLviewer;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 *
 * @author Ched
 */
public class CompilerDesign {

    public static void main(String[] args) throws IOException {
        Parser parse = new Parser();
        int lexLine = 0;
        System.out.println("============================================================ START LEXICAL ===========================================================");
        //rwTbl = ReservedWords.rWTable();
        Lexical lex = new Lexical(parse.rwTbl, parse.idTbl, parse.opTbl, "Test5.txt");

        while (Lexical.IsInputEnd()) {//while textfile is not empty
            Token currentTok = lex.getToken();//gets token one by one

            if (currentTok.getTokenType() == TokenKind.ERROR) {
                System.out.println(lex.errMsg);
                System.out.println("\nSYNTAX ERROR: FOUND AT LINE " + Lexical.line);
                lexLine = Lexical.line;
                break;
            } else {
                switch (currentTok.getTokenType()) {//getTokenType can be found in Token.java
                    case WHITESPACE:
                    case COMMENT:
                    case DLEFT:
                    case DRIGHT:
                    case SLEFT:
                    case SRIGHT:
                        break;
                    case NEWLINE:
                        System.out.println();
                        break;
                    case IGNORE:
                        System.out.println();
                        break;
                    case STRLIT:
                        TokenString strT = (TokenString) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + strT.literal + "] ");
                        break;
                    case CHARLIT:
                        TokenChar lit = (TokenChar) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + lit.literal + "] ");
                        break;
                    case UNSIGN_INTLIT:
                        TokenNum num = (TokenNum) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + num.num + "] ");
                        break;
                    case SIGN_INTLIT:
                        TokenSign num2 = (TokenSign) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + num2.num + "] ");
                        break;
                    case FLIT:
                        TokenFloat floater = (TokenFloat) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + floater.num + "] ");
                        break;
                    case BOOL_CONST:
                        TokenBool booli = (TokenBool) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + booli.lit + "] ");
                        break;
                    case ID:
                        TokenID tid = (TokenID) currentTok;
                        System.out.print("[" + currentTok.getTokenType() + " = " + tid.idName + "] ");
                        break;
                    default:
                        System.out.print("[" + currentTok.getTokenType() + "] ");
                }
            }
        }
        System.out.println();
        System.out.println("============================================================ END OF LEXICAL ==========================================================");

        System.out.println();
        System.out.println("============================================================ START PARSING ===========================================================");
        Lexical lex2 = new Lexical(parse.rwTbl, parse.idTbl, parse.opTbl, "Try.txt");
//        Parser parse = new Parser();
        String tree = parse.start(lex2);
        
        System.out.println("RECURSIVE DESCENT ACTIONS:");
        for (String act : parse.action) {
            System.out.println(act);
        }

        if (tree.contains("ERROR") || lex2.errMsg != null) {
            System.out.println("\nPARSING ERROR!");
            System.out.println("============================================================ END OF PARSING ==========================================================\n");
            int count = 0;
            if (lex2.errMsg != null) {
                System.out.println("LEXICAL ERROR");
                System.out.println(lex2.errMsg + "\nSYNTAX ERROR: FOUND AT LINE " + lexLine + "\n");
                count++;
            }
            for (int i = 0; i < parse.msg.size(); i++) {
                System.out.println(parse.msg.get(i) + "AT LINE NUMBER: " + parse.line.get(i) + "\n");
                count++;
            }
            System.out.println("Total Errors found: " + count);
        } else {
            System.out.println("\nSUCCESSFUL PARSE!");
            System.out.println("============================================================ END OF PARSING ==========================================================\n");
            System.out.println("============================================================ PARSE TREE ==============================================================");
            System.out.println(tree);
            String fileName = "C:\\Users\\Ched\\Documents\\NetBeansProjects\\CompilerDesign_1\\grtree.txt";
            try (FileWriter file = new FileWriter(fileName)) {
                file.write(tree);
            }
            new NPLviewer(fileName);
            System.out.println("============================================================ END OF PARSE TREE =======================================================");
        }

    }
}
