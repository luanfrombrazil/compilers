
import java.util.ArrayList;
// import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ArrayList<String> symbolTable = new ArrayList<>();
        // ArrayList<TokenQueue> queues = new ArrayList<>();
        LexicalChecker lc = new LexicalChecker();
        Scanner scs = new Scanner(System.in);
        while (true) {
            String line = scs.nextLine();
            if (line.equals("sair")) {
                break;
            }
            TokenQueue tq = lc.tokenizer(line, symbolTable);
            System.out.println("Fila: " + tq);

            SyntaxTree st = new SyntaxTree(symbolTable);
            st.processQueue(tq);
            System.out.println(st);
        }
        scs.close();
        /*
         * try (BufferedReader br = new BufferedReader(new FileReader("../util/input")))
         * {
         * String linha;
         * while ((linha = br.readLine()) != null) {
         * queues.add(lc.tokenizer(linha, symbolTable));
         * }
         * 
         * } catch (IOException e) {
         * System.err.println("ERRO AO LER O ARQUIVO DE TESTE: " + e.getMessage());
         * }
         * //printCases(queues);
         */
    }

    // static void printCases(List resultados) {
    //     System.out.print("\033[1;33mATENÇÃO: STOPWORDS SERÃO COMPLETAMENTE DESCARTADAS PELO INTERPRETADOR\033[0m");
    //     resultados.forEach(entry -> {
    //         System.out.print("\n\nFila de Tokens: \033[0;32m" + entry + "\033[0m");
    //     });
    // }

}
