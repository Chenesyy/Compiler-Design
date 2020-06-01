package compilerdesign;

import TokenLib.TokenKind;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 *
 * @author Ched
 */
public class ReservedWords {

    public static HashMap<String, TokenKind> rWTable() {
        HashMap<String, TokenKind> reservedWords = new HashMap<String, TokenKind>();
        reservedWords.put("SEED", TokenKind.SEED);
        reservedWords.put("HARVEST", TokenKind.HARV);

        reservedWords.put("int", TokenKind.INT);
        reservedWords.put("float", TokenKind.FLOAT);
        reservedWords.put("strbean", TokenKind.STRBEAN);
        reservedWords.put("mungbean", TokenKind.MUNGBEAN);
        reservedWords.put("bool", TokenKind.BOOLEAN);

        reservedWords.put("uproot", TokenKind.UPROOT);
        reservedWords.put("grow", TokenKind.GROW);
        reservedWords.put("plant", TokenKind.PLANT);
        reservedWords.put("sprout", TokenKind.SPROUT);

        reservedWords.put("if", TokenKind.IF);
        reservedWords.put("else", TokenKind.ELSE);
        reservedWords.put("elif", TokenKind.ELIF);
        reservedWords.put("while", TokenKind.WHILE);

        reservedWords.put("toString", TokenKind.TOSTRING);
        reservedWords.put("toInt", TokenKind.TOINT);
        reservedWords.put("length", TokenKind.LENGTH);

        return reservedWords;
    }
}
