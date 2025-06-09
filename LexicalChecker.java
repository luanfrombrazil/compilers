import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LexicalChecker {

    static final Set<String> RESERVADAS = new HashSet<>();
    static final Set<String> STOPWORDS = new HashSet<>();
    static final Set<String> INVALID_CARACTERS = new HashSet<>();

    public LexicalChecker() {
        try {
            Files.lines(Paths.get("./util/stopwords-pt.txt")).map(String::toLowerCase).forEach(STOPWORDS::add);

        } catch (IOException e) {
            System.err.println("ERRO AO CARREGAR AS STOPWORDS: " + e.getMessage());
        }
    }

    public TokenQueue tokenizer(String linha, ArrayList<String> symbolTable) {
        boolean stringIncoming = false;
        TokenQueue auxTokenQueue = new TokenQueue();
        StringBuilder sb = new StringBuilder();

        for (char c : linha.toCharArray()) {

            if (isQuote(c)) {
                String token = sb.toString();
                sb = new StringBuilder();
                if ((!isStopword(token) && !token.equals("")) || stringIncoming) {
                    auxTokenQueue.enqueue(token);
                }
                auxTokenQueue.enqueue(String.valueOf(c));
                stringIncoming = !stringIncoming;
                continue;
            }

            if (stringIncoming) {
                sb.append(c);
                continue;
            }

            if (Character.isLetter(c) || Character.isDigit(c)) {
                sb.append(c);
                continue;
            }

            if (isSeparator(c)) {
                String token = sb.toString();
                sb = new StringBuilder();
                if (!isStopword(token) && !token.equals("")) {
                    auxTokenQueue.enqueue(token);
                }
                auxTokenQueue.enqueue(String.valueOf(c));
                continue;
            }

            if (c == ' ') {
                String token = sb.toString();
                sb = new StringBuilder();
                if (!isStopword(token) && !token.equals("")) {
                    auxTokenQueue.enqueue(token);
                }
                continue;
            }

            INVALID_CARACTERS.add(String.valueOf(c));
        }
        String token = sb.toString();
        if (!isStopword(token) && !token.equals("")) {
            auxTokenQueue.enqueue(token);
        }

        if (stringIncoming) {
            System.err.println("ASPAS NAO ENCONTRADAS!");
            stringIncoming = !stringIncoming;
        }

        TokenQueue tokenQueue = new TokenQueue();

        while (!auxTokenQueue.isEmpty()) {
            token = auxTokenQueue.dequeue().toLowerCase();
            tokenQueue.enqueue(token);

            if (!contains(token, symbolTable) && token.matches(".*[a-zA-Z].*") && !stringIncoming) {
                symbolTable.add(token);
            } else if (isQuote(token.charAt(0))) {
                stringIncoming = !stringIncoming;
            }
        }
        return tokenQueue;
    }

    public static boolean isStopword(String token) {
        return STOPWORDS.contains(token.toLowerCase());
    }

    public static boolean isReserved(String token) {
        return RESERVADAS.contains(token.toLowerCase());
    }

    public static boolean isSeparator(char c) {
        char[] separadores = { '.', ',', '?', '!', '*', '/', ':', '-', '+', '(', ')', '_', '[', ']', '{', '}', '@' };
        boolean isSeparator = false;
        for (char e : separadores) {
            if (c == e) {
                isSeparator = true;
            }
        }
        return isSeparator;
    }

    public static boolean isQuote(char c) {
        return c == '“' || c == '”' || c == '"';
    }

    public static boolean contains(String token, ArrayList<String> symbolTable) {
        for (String elemento : symbolTable) {
            if (levenshtein(token, elemento) <= 2) {
                return true;
            }
        }
        return false;
    }

    static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
