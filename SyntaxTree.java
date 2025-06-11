
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class SyntaxTree {

    enum tipos {
        EXTENSOES, DATA_RELATIVA, FILTRO_DE_DATA, FILTRO_DE_TAMANHO, FILTRO_DE_DIRETORIO, UNIDADES_TAMANHO, TAMANHO
    };

    private static String token;
    private static boolean malformation;

    TreeCell root;
    ArrayList<String> symbolTable;

    static final Set<String> EXTENSOES = new HashSet<>();
    static final Set<String> DATA_RELATIVA = new HashSet<>();
    static final Set<String> COMANDOS_DE_BUSCA = new HashSet<>();
    static final Set<String> COMANDOS_DE_DIRETORIO = new HashSet<>();
    static final Set<String> COMANDOS_DE_DATA = new HashSet<>();
    static final Set<String> COMANDOS_DE_TAMANHO = new HashSet<>();
    static final Set<String> FILTRO_DE_DATA = new HashSet<>();
    static final Set<String> FILTRO_DE_TAMANHO = new HashSet<>();
    static final Set<String> FILTRO_DE_DIRETORIO = new HashSet<>();
    static final Set<String> UNIDADES_TAMANHO = new HashSet<>();

    class TreeCell {

        String tipo;
        List<String> tokens;
        List<TreeCell> filhos;

        TreeCell(List<String> tokens, String tipo) {
            this.tokens = tokens;
            this.tipo = tipo;
            filhos = new ArrayList<TreeCell>();
        }

        @Override
        public String toString() {
            return toString(0);
        }

        private String toString(int nivel) {
            StringBuilder sb = new StringBuilder();
            String indent = "\t".repeat(nivel);

            sb.append(indent)
                    .append("Tipo: ").append(tipo)
                    .append(", Tokens: ");
            if (tokens.isEmpty() && nivel != 0) {
                sb.append("TODOS OS ARQUIVOS").append("\n");
            } else {
                sb.append(tokens).append("\n");
            }

            for (TreeCell filho : filhos) {
                sb.append(filho.toString(nivel + 1));
            }
            return sb.toString();
        }
    }

    public boolean append(SyntaxTree arvore) {
        this.root.tipo = "RAIZ";
        if (arvore.root == null) {
            return false;
        }
        this.root.filhos.add(arvore.root);
        this.symbolTable.addAll(arvore.symbolTable);
        return true;
    }

    public SyntaxTree(ArrayList<String> symbolTable) {
        loadKeywords();
        this.root = new TreeCell(new ArrayList<String>(), "NULL");
        this.symbolTable = symbolTable;

    }

    public SyntaxTree() {
        loadKeywords();
        this.root = new TreeCell(new ArrayList<String>(), "NULL");
        this.symbolTable = new ArrayList<>();
    }

    public void processQueue(TokenQueue queue) {
        TokenQueue aux = queue.copy();

        while (!aux.isEmpty()) {
            token = aux.dequeue();
            if (contains(token, COMANDOS_DE_BUSCA, 2)) {
                this.root.tipo = "BUSCA";
                String comando = token;

                List<String> nome_arquivo = new ArrayList<>();
                List<String> extensao = new ArrayList<>();

                if (aux.isEmpty()) {
                    System.err.print("NOME E EXTENSÃO EXPERADOS\n");
                    malformation = true;
                    break;
                } else {
                    token = aux.dequeue();
                }

                while (!token.equals(".") && !aux.isEmpty()) {
                    nome_arquivo.add(token);
                    token = aux.dequeue();
                }

                if (!token.equals(".")) {
                    System.err.print(". EXPERADO\n");
                    malformation = true;
                    break;
                } else {
                    if (aux.isEmpty()) {
                        System.err.print("EXTENSÃO EXPERADA\n");
                        boolean recebido = askFor(tipos.EXTENSOES, aux);
                        if (!recebido) {
                            malformation = true;
                            break;
                        }
                    }
                    token = aux.dequeue();
                    if (contains(token, EXTENSOES, 1)) {
                        extensao.add(token);
                    } else {
                        System.err.print("EXTENSÃO DESCONHECIDA\n");
                        malformation = true;
                        break;
                    }
                }

                TreeCell busca_branch = new TreeCell(new ArrayList<String>(), "DOCUMENTO");
                busca_branch.tokens.add("arquivo");
                busca_branch.filhos.add(new TreeCell(nome_arquivo, "NOME_DOCUMENTO"));
                busca_branch.filhos.add(new TreeCell(extensao, "EXTENSAO_DOCUMENTO"));
                root.filhos.add(busca_branch);
                root.tokens.add(comando);

                if (!aux.isEmpty()) {
                    token = aux.dequeue();
                    TreeCell filtro = new TreeCell(new ArrayList<>(), "FILTRO");
                    if (contains(token, FILTRO_DE_DATA, 2)) {

                        filtro.tipo += "_DATA";
                        filtro.tokens.add(token);
                        TreeCell data_inicial = new TreeCell(new ArrayList<>(), "DATA_INICIAL");
                        TreeCell data_final = new TreeCell(new ArrayList<>(), "DATA_FINAL");
                        data_final.tokens.add(processaData("hoje"));
                        if (aux.isEmpty()) {
                            System.err.print("DATA EXPERADA\n");
                            boolean recebido = askFor(tipos.DATA_RELATIVA, aux);
                            if (!recebido) {
                                malformation = true;
                                break;
                            }
                        }
                        token = aux.dequeue();
                        if (contains(token, DATA_RELATIVA, 1)) {
                            data_inicial.tokens.add(processaData(token));
                        } else {
                            System.err.println("TERMO PARA DATA DESCONHECIDO\n");
                            malformation = true;
                            break;
                        }
                        root.tipo += "_FILTRADA";
                        this.root.filhos.add(filtro);
                        filtro.filhos.add(data_inicial);
                        filtro.filhos.add(data_final);

                    } else if (contains(token, FILTRO_DE_DIRETORIO, 2) && !aux.isEmpty()) {

                        filtro.tipo += "_DIRETORIO";
                        filtro.tokens.add(token);
                        TreeCell caminho = new TreeCell(new ArrayList<>(), "CAMINHO_DIRETORIO");

                        token = aux.dequeue();
                        if (!isQuote(token.charAt(0))) {
                            System.err.print("ASPAS EXPERADAS ANTES DO CAMINHO\n");
                            malformation = true;
                            break;
                        } else {
                            if (aux.isEmpty()) {
                                System.err.print("EXTENSÃO EXPERADA\n");
                                boolean recebido = askFor(tipos.EXTENSOES, aux);
                                if (!recebido) {
                                    malformation = true;
                                    break;
                                }

                            }
                            token = aux.dequeue();
                            caminho.tokens.add(token);
                        }

                        if (aux.isEmpty() || !isQuote(aux.dequeue().charAt(0))) {
                            System.err.print("ASPAS EXPERADAS APÓS O CAMINHO");
                            malformation = true;
                            break;
                        }

                        root.tipo += "_FILTRADA";
                        this.root.filhos.add(filtro);
                        filtro.filhos.add(caminho);

                    } else if (contains(token, FILTRO_DE_TAMANHO, 1)) {

                        filtro.tipo += "_TAMANHO";
                        filtro.tokens.add(token);
                        TreeCell tamanho = new TreeCell(new ArrayList<>(), "VALOR_TAMANHO");
                        TreeCell unidade = new TreeCell(new ArrayList<>(), "UNIDADE");

                        if (aux.isEmpty()) {
                            System.err.println("VALOR NUMERICO ESPERADO\n");
                            boolean recebido = askFor(tipos.TAMANHO, aux);
                            if (!recebido) {
                                malformation = true;
                                break;
                            }
                        }
                        token = aux.dequeue();

                        if (token.matches("\\d+")) {
                            tamanho.tokens.add(token);
                        } else {
                            System.err.println("TAMANHO DEVE SER VALOR NUMERICO\n");
                            malformation = true;
                            break;
                        }

                        if (aux.isEmpty()) {
                            System.err.println("UNIDADE DE TAMANHO ESPERADA\n");
                            boolean recebido = askFor(tipos.UNIDADES_TAMANHO, aux);
                            if (!recebido) {
                                malformation = true;
                                break;
                            }
                        }

                        token = aux.dequeue();
                        if (contains(token, UNIDADES_TAMANHO, 0)) {
                            unidade.tokens.add(token);
                        } else {
                            System.err.println("UNIDADE INVÁLIDA OU DESCONHECIDA" + token + "\n");
                            malformation = true;
                            break;

                        }
                        root.tipo += "_FILTRADA";
                        this.root.filhos.add(filtro);
                        filtro.filhos.add(tamanho);
                        filtro.filhos.add(unidade);
                        break;
                    } else {
                        dunno();
                        malformation = true;
                        break;
                    }
                }

            } else if (token.equals("qual")) {
                // CONSOME O TOKEN QUAL
            } else if (contains(token, COMANDOS_DE_DIRETORIO, 1)) {
                this.root.tipo = "CAMINHO_DE";
                String comando = token;

                List<String> nome_arquivo = new ArrayList<>();
                List<String> extensao = new ArrayList<>();

                if (aux.isEmpty()) {
                    System.err.print("NOME E EXTENSÃO EXPERADOS");
                    malformation = true;
                    break;
                } else {
                    token = aux.dequeue();
                }

                while (!token.equals(".") && !aux.isEmpty()) {
                    nome_arquivo.add(token);
                    token = aux.dequeue();
                }

                if (!token.equals(".")) {
                    System.err.print(". EXPERADO");
                    malformation = true;
                    break;
                } else {
                    if (aux.isEmpty()) {
                        System.err.print("EXTENSÃO EXPERADA");
                        boolean recebido = askFor(tipos.EXTENSOES, aux);
                        if (!recebido) {
                            malformation = true;
                            break;
                        }
                    }
                    token = aux.dequeue();
                    if (contains(token, EXTENSOES, 1)) {
                        extensao.add(token);
                    } else {
                        System.err.print("EXTENSÃO DESCONHECIDA");
                        malformation = true;
                        break;
                    }
                }

                TreeCell busca_branch = new TreeCell(new ArrayList<String>(), "DOCUMENTO");
                busca_branch.tokens.add(comando);
                busca_branch.filhos.add(new TreeCell(nome_arquivo, "NOME_DOCUMENTO"));
                busca_branch.filhos.add(new TreeCell(extensao, "EXTENSAO_DOCUMENTO"));
                root.filhos.add(busca_branch);
                break;
            } else if (contains(token, COMANDOS_DE_DATA, 1)) {
                this.root.tipo = "DATA_DE";
                String comando = token;

                List<String> nome_arquivo = new ArrayList<>();
                List<String> extensao = new ArrayList<>();

                if (aux.isEmpty()) {
                    System.err.print("NOME E EXTENSÃO EXPERADOS");
                    malformation = true;
                    break;
                } else {
                    token = aux.dequeue();
                }

                while (!token.equals(".") && !aux.isEmpty()) {
                    nome_arquivo.add(token);
                    token = aux.dequeue();
                }

                if (!token.equals(".")) {
                    System.err.print(". EXPERADO");
                    malformation = true;
                    break;
                } else {
                    if (aux.isEmpty()) {
                        System.err.print("EXTENSÃO EXPERADA");
                        boolean recebido = askFor(tipos.EXTENSOES, aux);
                        if (!recebido) {
                            malformation = true;
                            break;
                        }
                    }
                    token = aux.dequeue();
                    if (contains(token, EXTENSOES, 1)) {
                        extensao.add(token);
                    } else {
                        System.err.print("EXTENSÃO DESCONHECIDA");
                        malformation = true;
                        break;
                    }
                }

                TreeCell busca_branch = new TreeCell(new ArrayList<String>(), "DOCUMENTO");
                busca_branch.tokens.add(comando);
                busca_branch.filhos.add(new TreeCell(nome_arquivo, "NOME_DOCUMENTO"));
                busca_branch.filhos.add(new TreeCell(extensao, "EXTENSAO_DOCUMENTO"));
                root.filhos.add(busca_branch);
                break;
            } else if (contains(token, COMANDOS_DE_TAMANHO, 1)) {
                this.root.tipo = "TAMANHO_DE";
                String comando = token;

                List<String> nome_arquivo = new ArrayList<>();
                List<String> extensao = new ArrayList<>();

                if (aux.isEmpty()) {
                    System.err.print("NOME E EXTENSÃO EXPERADOS");
                    malformation = true;
                    break;
                } else {
                    token = aux.dequeue();
                }

                while (!token.equals(".") && !aux.isEmpty()) {
                    nome_arquivo.add(token);
                    token = aux.dequeue();
                }

                if (!token.equals(".")) {
                    System.err.print(". EXPERADO");
                    malformation = true;
                    break;
                } else {
                    if (aux.isEmpty()) {
                        System.err.print("EXTENSÃO EXPERADA");
                        boolean recebido = askFor(tipos.EXTENSOES, aux);
                        if (!recebido) {
                            malformation = true;
                            break;
                        }
                    }
                    token = aux.dequeue();
                    if (contains(token, EXTENSOES, 1)) {
                        extensao.add(token);
                    } else {
                        System.err.print("EXTENSÃO DESCONHECIDA");
                        malformation = true;
                        break;
                    }
                }

                TreeCell busca_branch = new TreeCell(new ArrayList<String>(), "DOCUMENTO");
                busca_branch.tokens.add(comando);
                busca_branch.filhos.add(new TreeCell(nome_arquivo, "NOME_DOCUMENTO"));
                busca_branch.filhos.add(new TreeCell(extensao, "EXTENSAO_DOCUMENTO"));
                root.filhos.add(busca_branch);
                break;
            } else {
                dunno();
            }
        }
    }

    private void dunno() {
        System.err.println("Não entendi");
    }

    private void loadKeywords() {
        try {
            Files.lines(Paths.get("./util/reserveds/searchinputs")).map(String::toLowerCase)
                    .forEach(COMANDOS_DE_BUSCA::add);
            Files.lines(Paths.get("./util/reserveds/pathinputs")).map(String::toLowerCase)
                    .forEach(COMANDOS_DE_DIRETORIO::add);
            Files.lines(Paths.get("./util/reserveds/dateinputs")).map(String::toLowerCase)
                    .forEach(COMANDOS_DE_DATA::add);
            Files.lines(Paths.get("./util/reserveds/sizeinputs")).map(String::toLowerCase)
                    .forEach(COMANDOS_DE_TAMANHO::add);

            Files.lines(Paths.get("./util/reserveds/extensions")).map(String::toLowerCase).forEach(EXTENSOES::add);
            Files.lines(Paths.get("./util/reserveds/relativedate")).map(String::toLowerCase)
                    .forEach(DATA_RELATIVA::add);

            Files.lines(Paths.get("./util/reserveds/datefilter")).map(String::toLowerCase).forEach(FILTRO_DE_DATA::add);
            Files.lines(Paths.get("./util/reserveds/sizefilter")).map(String::toLowerCase)
                    .forEach(FILTRO_DE_TAMANHO::add);
            Files.lines(Paths.get("./util/reserveds/pathfilter")).map(String::toLowerCase)
                    .forEach(FILTRO_DE_DIRETORIO::add);
            Files.lines(Paths.get("./util/reserveds/sizeunits")).map(String::toLowerCase)
                    .forEach(UNIDADES_TAMANHO::add);
        } catch (IOException e) {
            System.err.println("ERRO AO CARREGAR AS KEYWORDS: " + e.getMessage());
        }
    }

    private static boolean contains(String palavra, Set<String> reservadas, int tolerancia) {
        for (String elemento : reservadas) {
            if (levenshtein(palavra, elemento) <= tolerancia) {
                token = elemento;
                return true;
            }
        }
        return false;
    }

    private static int levenshtein(String a, String b) {
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

    public boolean isMalformed() {
        return malformation;
    }

    public static String processaData(String valor) {
        LocalDateTime hoje = LocalDateTime.now();

        switch (valor) {
            case "hoje" -> {
                return hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "anteontem" -> {
                return hoje.minusDays(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "dia" -> {
                return hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "ontem" -> {
                return hoje.minusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "semana" -> {
                return hoje.minusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "mes" -> {
                return hoje.minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "ano" -> {
                return hoje.minusYears(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            default ->
                System.err.println("TERMO PARA DATA DESCONHECIDO");
        }
        return "00/00/0000";
    }

    public boolean askFor(tipos tipo, TokenQueue queue, int timesAsked) {
        if (timesAsked > 2) {
            return false;
        }
        boolean recebido = true;

        @SuppressWarnings("resource")
        Scanner scs = new Scanner(System.in);
        LexicalChecker lexical = new LexicalChecker();
        TokenQueue tempQueue;
        switch (tipo) {
            case EXTENSOES:
                System.out.println("QUAL EXTENSÃO TEM O ARQUIVO?");
                tempQueue = lexical.tokenizer(scs.nextLine(), this.symbolTable);
                String extensao = tempQueue.dequeue();
                if (!contains(extensao, EXTENSOES, 0)) {
                    recebido = askFor(tipo, queue, timesAsked++);
                } else {
                    queue.skipQueue(extensao);
                }
                return recebido;
            case DATA_RELATIVA:
                System.out.println("QUAL A DATA?");
                tempQueue = lexical.tokenizer(scs.nextLine(), this.symbolTable);
                String data = tempQueue.dequeue();
                if (!contains(data, DATA_RELATIVA, 0)) {
                    recebido = askFor(tipo, queue, timesAsked++);
                } else {
                    queue.skipQueue(data);
                }
                return recebido;
            case UNIDADES_TAMANHO:
                System.out.println("QUAL UNIDADE DE TAMANHO? B, KB, MB, GB, TB?");
                tempQueue = lexical.tokenizer(scs.nextLine(), this.symbolTable);
                String unidade = tempQueue.dequeue();
                if (!contains(unidade, UNIDADES_TAMANHO, 0)) {
                    recebido = askFor(tipo, queue, timesAsked++);
                } else {
                    queue.skipQueue(unidade);
                }
                return recebido;
            case TAMANHO:
                System.out.println("QUAL TAMANHO DO ARQUIVO?");
                tempQueue = lexical.tokenizer(scs.nextLine(), this.symbolTable);
                String tamanho = tempQueue.dequeue();
                if (!tamanho.matches("\\d+")) {
                    recebido = askFor(tipo, queue, timesAsked++);
                } else {
                    queue.skipQueue(tamanho);
                }
                return recebido;
            default:
                return false;
        }
    }

    public boolean askFor(tipos tipo, TokenQueue queue) {
        return askFor(tipo, queue, 1);
    }

    public static boolean isQuote(char c) {
        return c == '“' || c == '”' || c == '"' || c == '"' || c == '\'';
    }

    @Override
    public String toString() {
        return this.root.toString();
    }
}
