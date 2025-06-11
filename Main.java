
import java.util.ArrayList;
// import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ArrayList<String> symbolTable = new ArrayList<>();
        // ArrayList<TokenQueue> queues = new ArrayList<>();
        LexicalChecker lc = new LexicalChecker();
        Scanner scs = new Scanner(System.in);
        SyntaxTree programRoot = new SyntaxTree();
        while (true) {
            String line = scs.nextLine();
            if (line.equals("sair")) {
                break;
            }
            TokenQueue tq = lc.tokenizer(line, symbolTable);
            //System.out.println("Fila: " + tq);

            SyntaxTree st = new SyntaxTree(symbolTable);
            st.processQueue(tq);
            if (!st.isMalformed()) {
                programRoot.append(st);
            }
        }
        System.out.println(programRoot);
        scs.close();
        
    }
}
