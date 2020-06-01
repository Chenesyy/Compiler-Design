package compilerdesign;

import TokenLib.Token;
import TokenLib.TokenBool;
import TokenLib.TokenChar;
import TokenLib.TokenFloat;
import TokenLib.TokenID;
import TokenLib.TokenKind;
import TokenLib.TokenNum;
import TokenLib.TokenSign;
import TokenLib.TokenString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 *
 * @author Ched
 */
public class Parser {//Tinanggal ko na lahat ng while (SMICOL), mali mali eh 

    public static char[][] prec = {
//in ADDSUB MULDIV EXPO ID SIGN UNSIGN LPAREN RPAREN SMICOL
        {'>', '<', '<', '<', '<', '<', '<', '>', '>'}, //ADDSUB
        {'>', '>', '<', '<', '<', '<', '<', '>', '>'}, //MULDIV
        {'>', '>', '<', '<', '<', '<', '<', '>', '>'}, //EXPO
        {'>', '>', '>', ' ', ' ', ' ', ' ', '>', '>'}, //ID
        {'>', '>', '>', ' ', ' ', ' ', ' ', '>', '>'}, //SIGN
        {'>', '>', '>', ' ', ' ', ' ', ' ', '>', '>'}, //UNSIGN
        {'<', '<', '<', '<', '<', '<', '<', '=', ' '}, //LPAREN
        {'>', '>', '>', ' ', ' ', ' ', ' ', '>', '>'}, //RPAREN
        {'<', '<', '<', '<', '<', '<', '<', ' ', 'a'} //SMICOL
    };                                                  //stk
    int err = 1;
    public HashMap<String, ArrayList<Object>> vars = new HashMap();
    public ArrayList<Integer> line = new ArrayList();
    public ArrayList<String> msg = new ArrayList();
    public ArrayList<String> action = new ArrayList();
    String tree = "[start # [";
    Token currentTok;
    public HashMap<String, TokenKind> rwTbl = ReservedWords.rWTable();
    public HashMap<String, ArrayList<Object>> idTbl = new HashMap();
    public HashMap<String, TokenKind> opTbl = new HashMap();
    //    public HashMap<String, TokenID> idTbl = new HashMap();
    String hold;
    boolean check = false;
    boolean pasok = true;

    public String start(Lexical lex) {
        action.add("METHOD CALL: <start>");
        advance(lex);
        switch (currentTok.getTokenType()) {
            case SEED:
                match(lex, TokenKind.SEED);
                match(lex, TokenKind.ID);
                match(lex, TokenKind.LCURL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                match(lex, TokenKind.HARV);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("ERROR IN START-OF-SEQUENCE\n");
                line.add(err);
                break;
        }
        return tree;
    }

    public void body(Lexical lex) {                                         //WORKING ON IT
        action.add("METHOD CALL: <body>");
        switch (currentTok.getTokenType()) {
            case ID:                                                      //NEEDS AYOS
                match(lex, TokenKind.ID);
                match(lex, TokenKind.ASSIGN);
                tree += "[assign # [";
                assign(lex);
                tree += "]],";
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case PLANT:
                match(lex, TokenKind.PLANT);
                match(lex, TokenKind.ID);
                tree += "[input_stmt2 # [";
                input_stmt2(lex);
                tree += "]],";
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case SPROUT:
                match(lex, TokenKind.SPROUT);
                tree += "[output_stmt2 # [";
                output_stmt2(lex);
                tree += "]],";
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case IF:
                match(lex, TokenKind.IF);
                match(lex, TokenKind.LPAREN);
                tree += "[cond_stmt2 # [";
                cond_stmt2(lex);
                tree += "]],";
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case WHILE:
                match(lex, TokenKind.WHILE);
                match(lex, TokenKind.LPAREN);
                tree += "[loop_stmt2 # [";
                loop_stmt2(lex);
                tree += "]],";
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case UNOP:
                match(lex, TokenKind.UNOP);
                match(lex, TokenKind.ID);
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case INT:
                match(lex, TokenKind.INT);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_int # [";
                stmt_int(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case FLOAT:
                match(lex, TokenKind.FLOAT);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_float # [";
                stmt_float(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case STRBEAN:
                match(lex, TokenKind.STRBEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_str # [";
                stmt_str(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case MUNGBEAN:
                match(lex, TokenKind.MUNGBEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_char # [";
                stmt_char(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            case BOOLEAN:
                match(lex, TokenKind.BOOLEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_bool # [";
                stmt_bool(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[body # [";
                body(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";                                         //EPSILON
                break;
        }
    }

    public void input_stmt2(Lexical lex) {
        action.add("METHOD CALL: <input_stmt2>");
        switch (currentTok.getTokenType()) {
            case SMICOL:
                match(lex, TokenKind.SMICOL);
                break;
            case INPUT:
                match(lex, TokenKind.INPUT);
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.SMICOL);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED ';' OR '<-'\n");
                line.add(err);
                break;
        }
    }
    
    public void output_stmt2(Lexical lex) {
        action.add("METHOD CALL: <output_stmt2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                tree += "[output_stmt4 # [";
                output_stmt4(lex);
                tree += "]],";
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case IGNORE:
                match(lex, TokenKind.IGNORE);
                tree += "[output_stmt4 # [";
                output_stmt4(lex);
                tree += "]],";
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case TOINT:
                match(lex, TokenKind.TOINT);
                match(lex, TokenKind.LPAREN);
                tree += "[to_int # [";
                to_int(lex);
                tree += "]],";
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case LENGTH:
                match(lex, TokenKind.LENGTH);
                match(lex, TokenKind.LPAREN);
                tree += "[length # [";
                length(lex);
                tree += "]],";
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            case TOSTRING:
                match(lex, TokenKind.TOSTRING);
                match(lex, TokenKind.LPAREN);
                tree += "[to_str # [";
                to_str(lex);
                tree += "]],";
                tree += "[output_stmt3 # [";
                output_stmt3(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("'" + currentTok.getTokenType() + "' CANNOT BE OUTPUTTED\n");
                line.add(err);
                break;
        }
    }

    public void output_stmt3(Lexical lex) {
        action.add("METHOD CALL: <output_stmt3>");
        switch (currentTok.getTokenType()) {
            case SMICOL:
                match(lex, TokenKind.SMICOL);
                break;
            case CONCAT:
                match(lex, TokenKind.CONCAT);
                tree += "[output_stmt2 # [";
                output_stmt2(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED ';' OR '~'\n");
                line.add(err);
                break;
        }
    }

    public void output_stmt4(Lexical lex) {
        action.add("METHOD CALL: <output_stmt4>");
        switch (currentTok.getTokenType()) {
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                tree += "[output_stmt4 # [";
                output_stmt4(lex);
                tree += "]],";
                break;
            case IGNORE:
                match(lex, TokenKind.IGNORE);
                tree += "[output_stmt4 # [";
                output_stmt4(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_int(Lexical lex) {
        action.add("METHOD CALL: <stmt_int>");
        switch (currentTok.getTokenType()) {
            case ASSIGN:
                match(lex, TokenKind.ASSIGN);
                tree += "[assign_int # [";
                assign_int(lex);
                tree += "]],";
                break;
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_float(Lexical lex) {
        action.add("METHOD CALL: <stmt_float>");
        switch (currentTok.getTokenType()) {
            case ASSIGN:
                match(lex, TokenKind.ASSIGN);
                tree += "[assign_float # [";
                assign_float(lex);
                tree += "]],";
                break;
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
/////                tree += " PARSING ERROR ";
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_str(Lexical lex) {
        action.add("METHOD CALL: <stmt_str>");
        switch (currentTok.getTokenType()) {
            case ASSIGN:
                match(lex, TokenKind.ASSIGN);
                tree += "[assign_str # [";
                assign_str(lex);
                tree += "]],";
                break;
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_char(Lexical lex) {
        action.add("METHOD CALL: <stmt_char>");
        switch (currentTok.getTokenType()) {
            case ASSIGN:
                match(lex, TokenKind.ASSIGN);
                tree += "[assign_char # [";
                assign_char(lex);
                tree += "]],";
                break;
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_bool(Lexical lex) {
        action.add("METHOD CALL: <stmt_bool>");
        switch (currentTok.getTokenType()) {
            case ASSIGN:
                match(lex, TokenKind.ASSIGN);
                tree += "[assign_bool # [";
                assign_bool(lex);
                tree += "]],";
                break;
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }
    
    public void assign(Lexical lex) {
        action.add("METHOD CALL: <assign>");
        Stack<TokenKind> stack = new Stack();
        String temp;
        switch (currentTok.getTokenType()) {
            case ID:
                if (currentTok.getTokenType() == TokenKind.ID) {
                    TokenID idINT = (TokenID) currentTok;
                    if (vars.containsKey(idINT.idName.trim())) {
                        advance2(lex);
                        if (currentTok.getTokenType() == TokenKind.SMICOL) {
                            tree += "[" + TokenKind.ID + " # []],";
                            action.add("MATCH TOKEN: [" + TokenKind.ID + " = " + idINT.idName.trim() + "] AND [" + TokenKind.ID + " = " + idINT.idName.trim() + "]"); 
                            action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        } else {
                            action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                            stack.push(TokenKind.SMICOL);
                            stack.push(TokenKind.ID);
                            tree += "[arith_expr # [";
                            //tree += arith_expr(lex, stack);
                            temp = arith_expr(lex, stack);
                            if (temp.contains("ERROR")) {
                                tree += " PARSING ERROR ";
//                                while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                    advance(lex);
//                                }
                            } else {
                                tree += temp;
                            }
                            tree += "]],";
                        }
                    } else {
                        tree += " PARSING ERROR ";
                        msg.add("ERROR IN USING ID '" + idINT.idName.trim() + "' (MIGHT NOT BEEN DECLARED)\n");
                        line.add(err);
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.ID + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.ID + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                match(lex, TokenKind.SMICOL);
                break;
            case SIGN_INTLIT:
                if (currentTok.getTokenType() == TokenKind.SIGN_INTLIT) {
                    TokenSign num2 = (TokenSign) currentTok;
                    advance2(lex);
                    if (currentTok.getTokenType() == TokenKind.SMICOL) {
                        tree += "[" + TokenKind.SIGN_INTLIT + " # []],";
                        action.add("MATCH TOKEN: [" + TokenKind.SIGN_INTLIT + " = " + num2.num + "] AND [" + TokenKind.SIGN_INTLIT + " = " + num2.num + "]");
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                    } else {
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        stack.push(TokenKind.SMICOL);
                        stack.push(TokenKind.SIGN_INTLIT);
                        tree += "[arith_expr # [";
                        //tree += arith_expr(lex, stack);
                        temp = arith_expr(lex, stack);
                        if (temp.contains("ERROR")) {
                            tree += " PARSING ERROR ";
//                            while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                advance(lex);
//                            }
                        } else {
                            tree += temp;
                        }
                        tree += "]],";
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.SIGN_INTLIT + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.SIGN_INTLIT + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                match(lex, TokenKind.SMICOL);
                break;
            case UNSIGN_INTLIT:
                if (currentTok.getTokenType() == TokenKind.UNSIGN_INTLIT) {
                    TokenNum num = (TokenNum) currentTok;
                    advance2(lex);
                    if (currentTok.getTokenType() == TokenKind.SMICOL) {
                        tree += "[" + TokenKind.UNSIGN_INTLIT + " # []],";
                        action.add("MATCH TOKEN: [" + TokenKind.UNSIGN_INTLIT + " = " + num.num + "] AND [" + TokenKind.UNSIGN_INTLIT + " = " + num.num + "]");  
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                    } else {
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        stack.push(TokenKind.SMICOL);
                        stack.push(TokenKind.UNSIGN_INTLIT);
                        tree += "[arith_expr # [";
                        //tree += arith_expr(lex, stack);
                        temp = arith_expr(lex, stack);
                        if (temp.contains("ERROR")) {
                            tree += " PARSING ERROR ";
//                            while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                advance(lex);
//                            }
                        } else {
                            tree += temp;
                        }
                        tree += "]],";
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.UNSIGN_INTLIT + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.UNSIGN_INTLIT + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                match(lex, TokenKind.SMICOL);
                break;
            case LPAREN:
                if (currentTok.getTokenType() == TokenKind.LPAREN) {           //No probs sa output
                    advance(lex);
                    stack.push(TokenKind.SMICOL);
                    stack.push(TokenKind.LPAREN);
                    tree += "[arith_expr # [";
                    //tree += arith_expr(lex, stack);
                    temp = arith_expr(lex, stack);
                    if (temp.contains("ERROR")) {
                        tree += " PARSING ERROR ";
//                        while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                            advance(lex);
//                        }
                    } else {
                        tree += temp;
                    }
                    tree += "]],";
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.LPAREN + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.LPAREN + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                } 
                match(lex, TokenKind.SMICOL);
                break;
            case FLIT: 
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.SMICOL);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.SMICOL);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.SMICOL);
                break;    
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                match(lex, TokenKind.SMICOL);
                break;
            case TOINT:
                match(lex, TokenKind.TOINT);
                match(lex, TokenKind.LPAREN);
                tree += "[to_int # [";
                to_int(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                break;
            case LENGTH:
                match(lex, TokenKind.LENGTH);
                match(lex, TokenKind.LPAREN);
                tree += "[length # [";
                length(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                break;
            case TOSTRING:
                match(lex, TokenKind.TOSTRING);
                match(lex, TokenKind.LPAREN);
                tree += "[to_str # [";
                to_str(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("ERROR IN INITIALIZING TO VARIABLE\n");
                line.add(err);
                break;
        }
    }

//    public void assign_int(Lexical lex) {
//        switch (currentTok.getTokenType()) {
//            case ID:
//                match(lex, TokenKind.ID);
//                break;
//            case SIGN_INTLIT:
//                match(lex, TokenKind.SIGN_INTLIT);
//                break;
//            case UNSIGN_INTLIT:
//                match(lex, TokenKind.UNSIGN_INTLIT);
//                break;
//            case TOINT:
//                match(lex, TokenKind.TOINT);
//                match(lex, TokenKind.LPAREN);
//                tree += "[to_int # [";
//                to_int(lex);
//                tree += "]],";
//                break;
//            case LENGTH:
//                match(lex, TokenKind.LENGTH);
//                match(lex, TokenKind.LPAREN);
//                tree += "[length # [";
//                length(lex);
//                tree += "]],";
//                break;
//            default:
//                tree += " PARSING ERROR: EXPECTED TYPE INT ";
//                break;
//        }
//    }
    public void assign_int(Lexical lex) {
        action.add("METHOD CALL: <assign_int>");
        Stack<TokenKind> stack = new Stack();
        String temp;
        switch (currentTok.getTokenType()) {
            case ID:
                if (currentTok.getTokenType() == TokenKind.ID) {               //Need pa ata ito ng action(MATCH) thing. pero mmya
                    TokenID idINT = (TokenID) currentTok;
                    if (vars.containsKey(idINT.idName.trim())) {
                        advance2(lex);
                        if (currentTok.getTokenType() == TokenKind.SMICOL) {
                            tree += "[" + TokenKind.ID + " # []],";
                            action.add("MATCH TOKEN: [" + TokenKind.ID + " = " + idINT.idName.trim() + "] AND [" + TokenKind.ID + " = " + idINT.idName.trim() + "]"); 
                            action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        } else {
                            action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                            stack.push(TokenKind.SMICOL);
                            stack.push(TokenKind.ID);
                            tree += "[arith_expr # [";
                            //tree += arith_expr(lex, stack);
                            temp = arith_expr(lex, stack);
                            if (temp.contains("ERROR")) {
                                tree += " PARSING ERROR ";
//                                while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                    advance(lex);
//                                }
                            } else {
                                tree += temp;
                            }
                            tree += "]],";
                        }
                    } else {
                        tree += " PARSING ERROR ";
                        msg.add("ERROR IN USING ID '" + idINT.idName.trim() + "' (MIGHT NOT BEEN DECLARED)\n");
                        line.add(err);
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.ID + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.ID + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                break;
            case SIGN_INTLIT:
                if (currentTok.getTokenType() == TokenKind.SIGN_INTLIT) {
                    TokenSign num2 = (TokenSign) currentTok;
                    advance2(lex);
                    if (currentTok.getTokenType() == TokenKind.SMICOL) {
                        tree += "[" + TokenKind.SIGN_INTLIT + " # []],";
                        action.add("MATCH TOKEN: [" + TokenKind.SIGN_INTLIT + " = " + num2.num + "] AND [" + TokenKind.SIGN_INTLIT + " = " + num2.num + "]");
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                    } else {
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        stack.push(TokenKind.SMICOL);
                        stack.push(TokenKind.SIGN_INTLIT);
                        tree += "[arith_expr # [";
                        //tree += arith_expr(lex, stack);
                        temp = arith_expr(lex, stack);
                        if (temp.contains("ERROR")) {
                            tree += " PARSING ERROR ";
//                            while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                advance(lex);
//                            }
                        } else {
                            tree += temp;
                        }
                        tree += "]],";
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.SIGN_INTLIT + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.SIGN_INTLIT + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                break;
            case UNSIGN_INTLIT:
                if (currentTok.getTokenType() == TokenKind.UNSIGN_INTLIT) {
                    TokenNum num = (TokenNum) currentTok;
                    advance2(lex);
                    if (currentTok.getTokenType() == TokenKind.SMICOL) {
                        tree += "[" + TokenKind.UNSIGN_INTLIT + " # []],";
                        action.add("MATCH TOKEN: [" + TokenKind.UNSIGN_INTLIT + " = " + num.num + "] AND [" + TokenKind.UNSIGN_INTLIT + " = " + num.num + "]");  
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                    } else {
                        action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
                        stack.push(TokenKind.SMICOL);
                        stack.push(TokenKind.UNSIGN_INTLIT);
                        tree += "[arith_expr # [";
                        //tree += arith_expr(lex, stack);
                        temp = arith_expr(lex, stack);
                        if (temp.contains("ERROR")) {
                            tree += " PARSING ERROR ";
//                            while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                                advance(lex);
//                            }
                        } else {
                            tree += temp;
                        }
                        tree += "]],";
                    }
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.UNSIGN_INTLIT + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.UNSIGN_INTLIT + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                }
                break;
            case LPAREN:
                if (currentTok.getTokenType() == TokenKind.LPAREN) {           //No probs sa output
                    advance(lex);
                    stack.push(TokenKind.SMICOL);
                    stack.push(TokenKind.LPAREN);
                    tree += "[arith_expr # [";
                    //tree += arith_expr(lex, stack);
                    temp = arith_expr(lex, stack);
                    if (temp.contains("ERROR")) {
                        tree += " PARSING ERROR ";
//                        while (currentTok.getTokenType() != TokenKind.SMICOL) {
//                            advance(lex);
//                        }
                    } else {
                        tree += temp;
                    }
                    tree += "]],";
                } else {
                    tree += " PARSING ERROR ";
                    action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + TokenKind.LPAREN + "]");
                    if (pasok == true) {
                        msg.add("MATCH ERROR: EXPECTED A\\AN [" + TokenKind.LPAREN + "]\n");
                        pasok = false;
                    }
                    line.add(err);
                } 
                break;
            case TOINT:
                match(lex, TokenKind.TOINT);
                match(lex, TokenKind.LPAREN);
                tree += "[to_int # [";
                to_int(lex);
                tree += "]],";
                break;
            case LENGTH:
                match(lex, TokenKind.LENGTH);
                match(lex, TokenKind.LPAREN);
                tree += "[length # [";
                length(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("INCOMPATIBLE TYPES: MUST ASSIGN TYPE INT VALUE\n");
                line.add(err);
                break;
        }
    }

    public void assign_float(Lexical lex) {
        action.add("METHOD CALL: <assign_float>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("INCOMPATIBLE TYPES: MUST ASSIGN TYPE FLOAT VALUE\n");
                line.add(err);
                break;
        }
    }

    public void assign_str(Lexical lex) {
        action.add("METHOD CALL: <assign_str>");
        switch (currentTok.getTokenType()) {
            case ID:          
                match(lex, TokenKind.ID);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                break;
            case TOSTRING:
                match(lex, TokenKind.TOSTRING);
                match(lex, TokenKind.LPAREN);
                tree += "[to_str # [";
                to_str(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("INCOMPATIBLE TYPES: MUST ASSIGN TYPE STRBEAN VALUE\n");
                line.add(err);
                break;
        }
    }

    public void assign_char(Lexical lex) {
        action.add("METHOD CALL: <assign_char>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("INCOMPATIBLE TYPES: MUST ASSIGN TYPE MUNGBEAN VALUE\n");
                line.add(err);
                break;
        }
    }

    public void assign_bool(Lexical lex) {
        action.add("METHOD CALL: <assign_bool>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("INCOMPATIBLE TYPES: MUST ASSIGN TYPE BOOL VALUE\n");
                line.add(err);
                break;
        }
    }

    public void multi_id(Lexical lex) {
        action.add("METHOD CALL: <multi_id>");
        switch (currentTok.getTokenType()) {
            case COMMA:
                match(lex, TokenKind.COMMA);
                matchID(lex, TokenKind.ID);
                tree += "[multi_id # [";
                multi_id(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void to_int(Lexical lex) {                                       //Baka need malaman laman ni STRLIT/CHARLIT
        action.add("METHOD CALL: <to_int>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                match(lex, TokenKind.RPAREN);
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.RPAREN);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.RPAREN);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.RPAREN);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED AN IDENTIFIER, STRLIT, CHARLIT OR FLIT\n");
                line.add(err);
                break;
        }
    }

    public void to_str(Lexical lex) {                                       //Baka need malaman laman ni STRLIT/CHARLIT
        action.add("METHOD CALL: <to_str>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                match(lex, TokenKind.RPAREN);
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                match(lex, TokenKind.RPAREN);
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                match(lex, TokenKind.RPAREN);
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.RPAREN);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.RPAREN);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED IDENTIFIER, STRLIT, CHARLIT, TYPE INT, OR TYPE FLOAT\n");
                line.add(err);
                break;

        }
    }

    public void length(Lexical lex) {
        action.add("METHOD CALL: <length>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                match(lex, TokenKind.RPAREN);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.RPAREN);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED STRBEAN IDENTIFIER OR STRLIT\n");
                line.add(err);
                break;
        }
    }

    public void loop_stmt2(Lexical lex) {
        action.add("METHOD CALL: <loop_stmt2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                tree += "[condition_id # [";
                condition_id(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED CONDITION STATEMENT\n");
                line.add(err);
                break;
        }
    }

    public void cond_stmt2(Lexical lex) {
        action.add("METHOD CALL: <cond_stmt2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                tree += "[condition_id # [";
                condition_id(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                match(lex, TokenKind.RPAREN);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                tree += "[elif_stmt # [";
                elif_stmt(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED CONDITION STATEMENT\n");
                line.add(err);
                break;
        }
    }

    public void condition_id(Lexical lex) {
        action.add("METHOD CALL: <condition_id>");
        switch (currentTok.getTokenType()) {
            case RELOP:
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                break;
            case MATCH:
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED A RELATIONAL OR MATCH OPERATOR\n");
                line.add(err);
                break;
        }
    }

    public void condition(Lexical lex) {
        action.add("METHOD CALL: <condition>");
        switch (currentTok.getTokenType()) {
            case LOGOP:
                match(lex, TokenKind.LOGOP);
                tree += "[log_expr2 # [";
                log_expr2(lex);
                tree += "]],";
                tree += "[condition # [";
                condition(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void stmt_repeat(Lexical lex) {
        action.add("METHOD CALL: <stmt_repeat>");
        switch (currentTok.getTokenType()) {
            case ID:                                                      //NEEDS AYOS
                match(lex, TokenKind.ID);
                match(lex, TokenKind.ASSIGN);
                tree += "[assign # [";
                assign(lex);
                tree += "]],";
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case UPROOT:
                match(lex, TokenKind.UPROOT);
                match(lex, TokenKind.SMICOL);
                break;
            case GROW:
                match(lex, TokenKind.GROW);
                match(lex, TokenKind.SMICOL);
                break;
            case PLANT:
                match(lex, TokenKind.PLANT);
                match(lex, TokenKind.ID);
                tree += "[input_stmt2 # [";
                input_stmt2(lex);
                tree += "]],";
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case SPROUT:
                match(lex, TokenKind.SPROUT);
                tree += "[output_stmt2 # [";
                output_stmt2(lex);
                tree += "]],";
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case IF:
                match(lex, TokenKind.IF);
                match(lex, TokenKind.LPAREN);
                tree += "[cond_stmt2 # [";
                cond_stmt2(lex);
                tree += "]],";
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case WHILE:
                match(lex, TokenKind.WHILE);
                match(lex, TokenKind.LPAREN);
                tree += "[loop_stmt2 # [";
                loop_stmt2(lex);
                tree += "]],";
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case UNOP:
                match(lex, TokenKind.UNOP);
                match(lex, TokenKind.ID);
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case INT:
                match(lex, TokenKind.INT);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_int # [";
                stmt_int(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case FLOAT:
                match(lex, TokenKind.FLOAT);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_float # [";
                stmt_float(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case STRBEAN:
                match(lex, TokenKind.STRBEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_str # [";
                stmt_str(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case MUNGBEAN:
                match(lex, TokenKind.MUNGBEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_char # [";
                stmt_char(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            case BOOLEAN:
                match(lex, TokenKind.BOOLEAN);
                matchID(lex, TokenKind.ID);
                tree += "[stmt_bool # [";
                stmt_bool(lex);
                tree += "]],";
                match(lex, TokenKind.SMICOL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void elif_stmt(Lexical lex) {
        action.add("METHOD CALL: <elif_stmt>");
        switch (currentTok.getTokenType()) {
            case ELIF:
                match(lex, TokenKind.ELIF);
                match(lex, TokenKind.LPAREN);
                tree += "[cond_stmt2 # [";
                cond_stmt2(lex);
                tree += "]],";
                break;
            case ELSE:
                match(lex, TokenKind.ELSE);
                match(lex, TokenKind.LCURL);
                tree += "[stmt_repeat # [";
                stmt_repeat(lex);
                tree += "]],";
                match(lex, TokenKind.RCURL);
                break;
            default:
                tree += "[ε # []],";
                break;
        }
    }

    public void string_match2(Lexical lex) {
        action.add("METHOD CALL: <string_match2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED AN IDENTIFIER, STRLIT, CHARLIT, OR TYPE BOOL\n");
                line.add(err);
                break;
        }
    }

    public void rel_expr2(Lexical lex) {
        action.add("METHOD CALL: <rel_expr2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED AN IDENTIFIER, TYPE INT, OR TYPE FLOAT\n");
                line.add(err);
                break;
        }
    }

    public void log_expr2(Lexical lex) {
        action.add("METHOD CALL: <log_expr2>");
        switch (currentTok.getTokenType()) {
            case ID:
                match(lex, TokenKind.ID);
                tree += "[log2_id # [";
                log2_id(lex);
                tree += "]],";
                break;
            case STRLIT:
                match(lex, TokenKind.STRLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                break;
            case CHARLIT:
                match(lex, TokenKind.CHARLIT);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                break;
            case BOOL_CONST:
                match(lex, TokenKind.BOOL_CONST);
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                break;
            case SIGN_INTLIT:
                match(lex, TokenKind.SIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                break;
            case UNSIGN_INTLIT:
                match(lex, TokenKind.UNSIGN_INTLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                break;
            case FLIT:
                match(lex, TokenKind.FLIT);
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED CONDITION STATEMENT\n");
                line.add(err);
                break;
        }
    }

    public void log2_id(Lexical lex) {
        action.add("METHOD CALL: <log2_id>");
        switch (currentTok.getTokenType()) {
            case MATCH:
                match(lex, TokenKind.MATCH);
                tree += "[string_match2 # [";
                string_match2(lex);
                tree += "]],";
                break;
            case RELOP:
                match(lex, TokenKind.RELOP);
                tree += "[rel_expr2 # [";
                rel_expr2(lex);
                tree += "]],";
                break;
            default:
                tree += " PARSING ERROR ";
                msg.add("EXPECTED A RELATIONAL OR MATCH OPERATOR\n");
                line.add(err);
                break;
        }
    }

    public String arith_expr(Lexical lex, Stack<TokenKind> stack) {
        action.add("METHOD CALL: <arith_expr>");
        LinkedList<String> list = new LinkedList();
        boolean accept = false;
        String twee = "";

        while (accept == false) {
            if (prec_tbl(stack.peek(), currentTok.getTokenType()) == ' ') {
                twee += " PARSING ERROR ";
                msg.add("INVALID ARITHMETIC EXPRESSION\n");
                line.add(err);
                accept = true;
            } else if (prec_tbl(stack.peek(), currentTok.getTokenType()) == 'a') {
                accept = true;
            } else if (prec_tbl(stack.peek(), currentTok.getTokenType()) == '<') {
                if (currentTok.getTokenType() == TokenKind.ID) {
                    TokenID arithID = (TokenID) currentTok;
                    if (vars.containsKey(arithID.idName.trim())) {
                        stack.push(currentTok.getTokenType());
                        advance(lex);
                    } else {
                        tree += " PARSING ERROR ";
                        msg.add("ERROR IN USING ID '" + arithID.idName.trim() + "' (MIGHT NOT BEEN DECLARED)\n");
                        line.add(err);
                        accept = true;
                    }
                } else {
                    stack.push(currentTok.getTokenType());
                    advance(lex);
                }
            } else if (prec_tbl(stack.peek(), currentTok.getTokenType()) == '>') {
                list.add(stack.pop().toString());
            } else if (prec_tbl(stack.peek(), currentTok.getTokenType()) == '=') {
                stack.pop();
                advance(lex);
            }
        }

        //twee = expr_tree(list);
        if (twee.contains("ERROR")) {
            twee = " PARSING ERROR ";
        } else {
            twee = expr_tree(list);   
        }

        return twee;
    }

    public String expr_tree(LinkedList<String> list) {
        LinkedList<String> exprTree = new LinkedList<String>();
        String code;
        String leaf;
        String left;
        String right;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals("ID") || list.get(i).equals("SIGN_INTLIT") || list.get(i).equals("UNSIGN_INTLIT")) {
                leaf = "[" + list.get(i) + " # []],";
                exprTree.push(leaf);
            } else {
                right = exprTree.pop();
                left = exprTree.pop();
                code = "[" + list.get(i) + " # [" + left + right + "]],";
                exprTree.push(code);
            }
        }

        return exprTree.pop();
    }

    public char prec_tbl(TokenKind left, TokenKind right) {
        HashMap<TokenKind, Integer> map = new HashMap<TokenKind, Integer>();
        map.put(TokenKind.ADDSUB, 0);
        map.put(TokenKind.MULDIV, 1);
        map.put(TokenKind.EXPO, 2);
        map.put(TokenKind.ID, 3);
        map.put(TokenKind.SIGN_INTLIT, 4);
        map.put(TokenKind.UNSIGN_INTLIT, 5);
        map.put(TokenKind.LPAREN, 6);
        map.put(TokenKind.RPAREN, 7);
        map.put(TokenKind.SMICOL, 8);
        //map.put(TokenKind.COMMA, 9);
        
        char move;     
        try {
            move = prec[map.get(left)][map.get(right)];
        } catch (Exception e) {
            move = ' ';
        }
        
        return move;

        //return prec[map.get(left)][map.get(right)];
    }
    
    public void match(Lexical lex, TokenKind expectedToken) {
        if (currentTok.getTokenType() == expectedToken) {
            tree += "[" + expectedToken + " # []],";
            if (expectedToken == TokenKind.ID || expectedToken == TokenKind.STRLIT || expectedToken == TokenKind.CHARLIT || expectedToken == TokenKind.FLIT || expectedToken == TokenKind.SIGN_INTLIT || expectedToken == TokenKind.UNSIGN_INTLIT || expectedToken == TokenKind.BOOL_CONST) {
                getLiteral(lex, expectedToken, "match");
            } else {
                action.add("MATCH TOKEN: [" + currentTok.getTokenType() + "] AND [" + expectedToken + "]");                
            }
            if (currentTok.getTokenType() == TokenKind.HARV) {
                tree += "]]";
            } else {
                advance(lex);
            }
        } else {
            tree += " PARSING ERROR ";
            action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + expectedToken + "]");
            if (pasok == true) {
                msg.add("MATCH ERROR: EXPECTED A\\AN [" + expectedToken + "]\n");
                pasok = false;
            }
            line.add(err);
        }
    }

    public void matchID(Lexical lex, TokenKind id) {
        if (currentTok.getTokenType() == id) {
            tree += "[" + id + " # []],";
            TokenID tid = (TokenID) currentTok;
            action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + tid.idName.trim() + "] AND [" + id + " = " + tid.idName.trim() + "]");  
            hold = tid.idName.trim();
            if (vars.containsKey(tid.idName.trim())) {          
                tree += " PARSING ERROR ";
                msg.add("'" + tid.idName.trim() + "' HAS ALREADY BEEN DECLARED\n");
                line.add(err);   
            } else {
                vars.computeIfAbsent(tid.idName.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(id));
                idTbl.computeIfAbsent(tid.idName.trim(), k -> new ArrayList<>()).addAll(Arrays.asList(tid.idName.trim()));
                advance(lex);
            }
        } else {
            tree += " DOES NOT MATCH ";
            action.add("MATCH ERROR: [" + currentTok.getTokenType() + "] AND [" + id + "]");
            if (pasok == true) {
                msg.add("MATCH ERROR: EXPECTED A\\AN [" + id + "]\n");
                pasok = false;
            }
            line.add(err);
        }
    }
    
    public void getLiteral(Lexical lex, TokenKind expectedToken, String ano) {
        switch (expectedToken) {
            case ID:
                TokenID tid = (TokenID) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + tid.idName.trim() + "] AND [" + expectedToken + " = " + tid.idName.trim() + "]");                      
                    if (check == true) {
                        if (!(vars.containsKey(tid.idName.trim()))) {
                            tree += " PARSING ERROR ";
                            msg.add("'" + tid.idName.trim() + "' HAS NOT BEEN DECLARED\n");
                            line.add(err);
                        }
                    }
                    check = true;
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + tid.idName.trim() + "]");          
                }
                break;
            case STRLIT:
                TokenString strT = (TokenString) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + strT.literal + "] AND [" + expectedToken + " = " + strT.literal + "]");                      
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + strT.literal + "]");
                }
                break;
            case CHARLIT:
                TokenChar lit = (TokenChar) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + lit.literal + "] AND [" + expectedToken + " = " + lit.literal + "]");  
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + lit.literal + "]");
                }
                break;
            case UNSIGN_INTLIT:
                TokenNum num = (TokenNum) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + num.num + "] AND [" + expectedToken + " = " + num.num + "]");  
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + num.num + "]");
                }
                break;
            case SIGN_INTLIT:
                TokenSign num2 = (TokenSign) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + num2.num + "] AND [" + expectedToken + " = " + num2.num + "]");  
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + num2.num + "]");
                }
                break;
            case FLIT:
                TokenFloat floater = (TokenFloat) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + floater.num + "] AND [" + expectedToken + " = " + floater.num + "]");  
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + floater.num + "]");
                }
                break;
            case BOOL_CONST:
                TokenBool booli = (TokenBool) currentTok;
                if (ano.equals("match")) {
                    action.add("MATCH TOKEN: [" + currentTok.getTokenType() + " = " + booli.lit + "] AND [" + expectedToken + " = " + booli.lit + "]");  
                } else if (ano.equals("look")) {
                    action.add("LOOKAHEAD: [" + currentTok.getTokenType() + " = " + booli.lit + "]");
                }
                break;
        }
    }
    
    public void advance2(Lexical lex) {
        currentTok = lex.getToken();
        while (currentTok.getTokenType() == TokenKind.WHITESPACE || currentTok.getTokenType() == TokenKind.COMMENT || currentTok.getTokenType() == TokenKind.NEWLINE || /*currentTok.getTokenType() == TokenKind.IGNORE*/ currentTok.getTokenType() == TokenKind.DLEFT || currentTok.getTokenType() == TokenKind.DRIGHT || currentTok.getTokenType() == TokenKind.SLEFT || currentTok.getTokenType() == TokenKind.SRIGHT || currentTok.getTokenType() == TokenKind.ERROR) {
            if (currentTok.getTokenType() == TokenKind.NEWLINE) {
                err++;
            }
            currentTok = lex.getToken();
        }
    }

    public boolean isNum(String str) {
        if (str == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    public boolean validFloat(String str) {
        if (str == null) {
            return false;
        }
        if (str.contains(".") && (str.startsWith("+") || str.startsWith("-"))) {
            return false;
        }
        return true;
    }
    
    public boolean isFloat(String str) {
        if (str == null) {
            return false;
        }
        if (str.contains(".")) {
            return true;
        }
        return false;
    }
    
    public void advance(Lexical lex) {
        currentTok = lex.getToken();
        while (currentTok.getTokenType() == TokenKind.WHITESPACE || currentTok.getTokenType() == TokenKind.COMMENT || currentTok.getTokenType() == TokenKind.NEWLINE || /*currentTok.getTokenType() == TokenKind.IGNORE*/ currentTok.getTokenType() == TokenKind.DLEFT || currentTok.getTokenType() == TokenKind.DRIGHT || currentTok.getTokenType() == TokenKind.SLEFT || currentTok.getTokenType() == TokenKind.SRIGHT || currentTok.getTokenType() == TokenKind.ERROR) {
            if (currentTok.getTokenType() == TokenKind.NEWLINE) {
                err++;
            }
            currentTok = lex.getToken();
        }
        if (currentTok.getTokenType() == TokenKind.ID || currentTok.getTokenType() == TokenKind.STRLIT || currentTok.getTokenType() == TokenKind.CHARLIT || currentTok.getTokenType() == TokenKind.FLIT || currentTok.getTokenType() == TokenKind.SIGN_INTLIT || currentTok.getTokenType() == TokenKind.UNSIGN_INTLIT || currentTok.getTokenType() == TokenKind.BOOL_CONST) {
                getLiteral(lex, currentTok.getTokenType(), "look");
        } else {
                action.add("LOOKAHEAD: [" + currentTok.getTokenType() + "]");
        }
    }

}