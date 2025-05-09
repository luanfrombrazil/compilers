/*

ATIVIDADE 1 - ANALISADOR LÉXICO

A etapa de análise léxica inclui as seguintes atividades para confecção do seu tradutor/interpretador/compilador:

FEITOS
1) Verificação léxica, com avaliação do alfabeto usado. Basicamente aceite letras de A-Z, a-z, 0-9, caracteres de pontuação e caracteres especiais presentes no teclado brasileiro. Indique a não identificação de caracteres especiais [OUTROS, NÃO DISPONÍVEIS NO TECLADO], visíveis ou invisíveis já na fase léxica, COM mensagem de erro de sua escolha.

2) Como se trata de linguagem natural, providencie uma lista de stopwords que possa ser mobilizada em seu chatbot. A lista pode ser obtida na Internet, de qualquer fonte (indique a fonte).

4) Todas as palavras presentes no texto do seu usuário que não sejam stopwords devem ser incluídos em uma estrutura de dados do tipo lista: uma Tabela de Símbolos. Implemente sua Tabela de Símbolos preliminar.

5) Todas as palavras no texto [EXCETO STOPWORDS] também devem ser armazenadas em uma fila de tokens para que a próxima fase avalie qual regra gramatical será invocada.

6) Faça um teste para queries/mensagens enviadas pelo usuário para o seu chatbot para se certificar sobre as palavras que efetivamente estão permanecendo no tradutor. Veja as filas produzidas para cada situação.

3) Crie uma função de similaridade de strings com erro para busca por palavras incorretas (grafia incorreta). Caso faça uso de um método pronto, indique a fonte ou crie um do zero. 
Ex: Exceção == Excecao == excessão == Esceção para um erro de até dois caracteres.

AINDA NAO FEITOS



 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    static final Set<String> RESERVADAS = new HashSet<>();
    static final Set<String> STOPWORDS = new HashSet<>();
    static final Set<String> INVALID_CARACTERS = new HashSet<>();

    public static void main(String[] args) {

        //CARREGANDO AS STOPWORDS PARA UM SET
        try {
            /*
             * AS STOPWORDS FORAM ENCONTRADAS NO SEGUINTE REPOSITÓRIO DO GITHUB:
             * https://github.com/stopwords-iso/stopwords-pt
             * 
             * ALGUMAS STOPWORDS FORAM REMOVIDAS, COMO A PALAVRA "primeiro", "primeira"
             * OUTRAS FORAM ADICIONADAS, GIRIAS DE ALGUMAS STOPWORDS, falou=flw, obrigado=obg=brigado=obrigadão,
             * 
             */
            Files.lines(Paths.get("../util/stopwords-pt.txt")).map(String::toLowerCase).forEach(STOPWORDS::add);
            Files.lines(Paths.get("../util/reserved-pt.txt")).map(String::toLowerCase).forEach(RESERVADAS::add);
        } catch (IOException e) {
            System.err.println("ERRO AO CARREGAR AS STOPWORDS: " + e.getMessage());
        }

        //LISTA DE TOKEN E TABELA PARA TESTAR MULTIPLAS ENTRADAS DE UMA UNICA VEZ
        //CADA LINHA DO INPUT É UMA ENTRADA DIFERENTE, NÃO SALTE LINHAS VAZIAS.
        List<Map.Entry<TokenQueue, ArrayList<String>>> resultados = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("../util/input"))) {
            String linha;

            //LENDO CADA LINHA DO INPUT
            while ((linha = br.readLine()) != null) {
                boolean stringIncoming = false;
                TokenQueue auxTokenQueue = new TokenQueue();
                StringBuilder sb = new StringBuilder();
                //VERIFICANDO CARACTER POR CARACTER DA LINHA E TOKENIZANDO
                for (char c : linha.toCharArray()) {
                    //DETECÇÃO DE UMA STRING, TODAS PALAVRAS DENTRO DE ASPAS SÃO COLOCADAS NA FILA DE TOKENS, MESMO QUE SEJAM STEPWORDS.
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
                    //CASO ONDE OS CARACTERES LIDOS SÃO DA STRING
                    if (stringIncoming) {
                        sb.append(c);
                        continue;
                    }

                    //CASO COMUM, É LETRA OU NUMERO
                    if (Character.isLetter(c) || Character.isDigit(c)) {
                        sb.append(c);
                        continue;
                    }

                    //CASO SEJA UM SEPARADOR/OPERADOR 
                    if (isSeparator(c)) {
                        String token = sb.toString();
                        sb = new StringBuilder();
                        if (!isStopword(token) && !token.equals("")) {
                            auxTokenQueue.enqueue(token);
                        }
                        auxTokenQueue.enqueue(String.valueOf(c));
                        continue;
                    }

                    //CASO ONDE O SEPARADOR É O ESPAÇO EM BRANCO.
                    if (c == ' ') {
                        String token = sb.toString();
                        sb = new StringBuilder();
                        if (!isStopword(token) && !token.equals("")) {
                            auxTokenQueue.enqueue(token);
                        }
                        continue;
                    }
                    //CARACTERES INVÁLIDOS NAO PARAM A EXECUÇÃO DO PROGRAMA, APENAS SÃO APRESENTADOS COMO INVÁLIDOS PARA O USUÁRIO NO FIM.
                    INVALID_CARACTERS.add(String.valueOf(c));
                }
                //CASO ONDE O USUÁRIO DEIXE DE FECHAR ASPAS.
                if (stringIncoming) {
                    System.err.println("ASPAS NAO ENCONTRADAS!");
                    stringIncoming = !stringIncoming;
                }

                //A PARTIR DAQUI A FILA DE TOKENS ESTÁ MONTADA E OS SIMBOLOS SERÃO ADICIONADOS NA TABELA.
                ArrayList<String> symbolTable = new ArrayList<>();

                TokenQueue tokenQueue = new TokenQueue();

                while (!auxTokenQueue.isEmpty()) {
                    String token = auxTokenQueue.dequeue();
                    tokenQueue.enqueue(token);

                    //NÃO É PALAVRA RESERVADA, NÃO ESTÁ "CONTIDA" OU NÃO HÁ SIMILARES
                    //ACEITA APENAS TOKENS QUE CONTENHAM LETRAS, CASO SEJA SOMENTE NÚMEROS, É IGNORADO.
                    //TODAS PALAVRAS DENTRO DE UMA STRING IGNORADAS.
                    if (!isReserved(token) && !contains(token, symbolTable) && token.matches(".*[a-zA-Z].*") && !stringIncoming) {
                        symbolTable.add(token);
                    } else if (isQuote(token.charAt(0))) {
                        stringIncoming = !stringIncoming;
                    }
                }
                
                //ADICIONA TODOS OS CASOS (TABELA DE SIMBOLOS E FILA DE TOKENS) EM UM ABSMAP PARA SER IMPRESSO NO FINAL
                resultados.add(new AbstractMap.SimpleEntry<>(tokenQueue, symbolTable));

            }
            //FIM DO TRATAMENTO DA LINHA DO ARQUIVO, REPETE EM OUTRA LINHA.
        } catch (IOException e) {
            System.err.println("ERRO AO LER O ARQUIVO DE TESTE: " + e.getMessage());
        }
        printCases(resultados);
    }


    //COMO ESTOU RODANDO OS CÓDIGOS VIA TERMINAL, UTILIZANDO O SCRIPT run.sh ESTOU UTILIZANDO \033[0;32m PARA MODIFICAR A COR DO TEXTO E MELHORAR A VISIBILIDADE
    //O ARQUIVO OUTPUT.TXT É GERADO CASO SE UTILIZE O RUN.SH.
    static void printCases(List<Map.Entry<TokenQueue, ArrayList<String>>> resultados) {
        System.out.print("\033[1;33mATENÇÃO: STOPWORDS SERÃO COMPLETAMENTE DESCARTADAS PELO INTERPRETADOR\033[0m");
        resultados.forEach(entry -> {
            System.out.print("\n\nFila de Tokens: \033[0;32m" + entry.getKey() + "\033[0m");
            System.out.print("\nTabela de Simbolos: \033[0;32m" + entry.getValue() + "\033[0m");
        });

        System.out.print("\n\033[0;31mOS CARACTERES: \033[0m");
        INVALID_CARACTERS.forEach(e -> System.out.print("\033[0;31m" + e + "\033[0m, "));
        System.out.print("\033[0;31m FORAM DETECTADOS NAS ENTRADAS E IGNORADOS PELO INTERPRETADOR.\033[0m\n\n");
    }

    //VERIFICA SE UM POSSIVEL TOKEN É STOPWORD
    static boolean isStopword(String token) {
        return STOPWORDS.contains(token.toLowerCase());
    }

    //VERIFICA SE UM POSSIVEL TOKEN É PALAVRA RESERVADA
    static boolean isReserved(String token) {
        return RESERVADAS.contains(token.toLowerCase());
    }

    //VERIFICA SE UM CARACTER É SEPADADOR
    static boolean isSeparator(char c) {
        char[] separadores = {'.', ',', '?', '!', '*', '/', ':', '-', '+', '(', ')', '_', '[', ']', '{', '}', '@'};
        boolean isSeparator = false;
        for (char e : separadores) {
            if (c == e) {
                isSeparator = true;
            }
        }
        return isSeparator;
    }

    //VERIFICA SE EXISTE O INICIO DE UMA STRING
    static boolean isQuote(char c) {
        return c == '“' || c == '”' || c == '"';
    }

    //ITERA A TABELA DE SIMBOLOS COMPARANDO COM O TOKEN A SER INSERIDO
    static boolean contains(String token, ArrayList<String> symbolTable) {
        for (String elemento : symbolTable) {
            if (levenshtein(token, elemento) <= 2) {
                return true;
            }
        }
        return false;
    }

    /*
    O MÉTODO UTILIZADO FOI A DISTANCIA DE LEVENSHTEIN, QUE MEDE A DIFERENÇA ENTRE DUAS STRINGS. 
    PELO NÚMERO MINIMO DE TRANSFORMAÇÕES NECESSÁRIAS PARA TRANSFORMA UMA STRING EM OUTRA.

    O CÓDIGO UTILIZADO FOI ADQUIRIDO NO SITE RosettaCode
    LINK: https://rosettacode.org/wiki/Levenshtein_distance. ACESSO EM: 2 DE MAIO DE 2025.
    
    O CÓDIGO ENCONTRADO NO SITE, TRANSFORMAVA A STRING EM LOWERCASE, MANTIVE SEM ISSO PARA TORNAR O MÉTODO CASE SENSITIVE. 
     */
    static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
