import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        LexicalChecker lc = new LexicalChecker();
        Scanner scs = new Scanner(System.in);
        SyntaxTree programRoot = new SyntaxTree();
        while (true) {
            String line = scs.nextLine();
            if (line.equals("sair")) {
                break;
            }
            TokenQueue tq = lc.tokenizer(line);
            SyntaxTree st = new SyntaxTree();
            st.processQueue(tq);
            if (!st.isMalformed()) {
                programRoot.append(st);
            }
        }
        System.out.println(programRoot);
        System.out.println("\n\n" + programRoot.symbolTable);
        scs.close();
    }
}
