import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Persistencia {
    private final Path arquivo = Paths.get("dados_series.json");
    private final JsonSimples json = new JsonSimples();
    private boolean dadosPadraoUsados;
    private String avisoCarregamento;

    public DadosUsuario carregarDados() {
        dadosPadraoUsados = false;
        avisoCarregamento = null;
        if (!Files.exists(arquivo)) return criarDadosIniciais();
        try {
            String texto = Files.readString(arquivo, StandardCharsets.UTF_8);
            if (texto.trim().isEmpty()) throw new Exception("arquivo vazio");
            Object raiz = json.ler(texto);
            if (!(raiz instanceof Map)) throw new Exception("objeto principal ausente");
            Map<?, ?> objeto = (Map<?, ?>) raiz;
            DadosUsuario dados = new DadosUsuario(new Usuario(texto(objeto.get("nomeUsuario"))));
            dados.setFavoritos(lerLista(objeto.get("favoritos")));
            dados.setAssistidas(lerLista(objeto.get("assistidas")));
            dados.setQueroAssistir(lerLista(objeto.get("queroAssistir")));
            return dados;
        } catch (Exception e) {
            avisoCarregamento = "O arquivo de dados estava invalido. Foram usados dados padrao.";
            return criarDadosIniciais();
        }
    }

    public boolean salvarDados(DadosUsuario dados) {
        try {
            Map<String, Object> raiz = new LinkedHashMap<String, Object>();
            raiz.put("nomeUsuario", dados.getUsuario().getNome());
            raiz.put("favoritos", escreverLista(dados.getFavoritos()));
            raiz.put("assistidas", escreverLista(dados.getAssistidas()));
            raiz.put("queroAssistir", escreverLista(dados.getQueroAssistir()));
            Files.writeString(arquivo, formatarJson(json.escrever(raiz)), StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private DadosUsuario criarDadosIniciais() {
        dadosPadraoUsados = true;
        DadosUsuario dados = new DadosUsuario(new Usuario("Usuario"));
        ArrayList<String> drama = new ArrayList<String>();
        drama.add("Drama");
        dados.adicionar(dados.getFavoritos(), new Serie(169, "Breaking Bad", "English", drama, 9.2,
                "Ended", "2008-01-20", "2013-09-29", "AMC"));
        ArrayList<String> comedia = new ArrayList<String>();
        comedia.add("Comedy");
        dados.adicionar(dados.getAssistidas(), new Serie(526, "The Office", "English", comedia, 8.5,
                "Ended", "2005-03-24", "2013-05-16", "NBC"));
        ArrayList<String> ficcao = new ArrayList<String>();
        ficcao.add("Drama"); ficcao.add("Science-Fiction");
        dados.adicionar(dados.getQueroAssistir(), new Serie(2993, "Stranger Things", "English", ficcao, 8.6,
                "Running", "2016-07-15", "", "Netflix"));
        return dados;
    }

    private ArrayList<Serie> lerLista(Object valor) {
        ArrayList<Serie> lista = new ArrayList<Serie>();
        if (!(valor instanceof ArrayList)) return lista;
        for (Object item : (ArrayList<?>) valor) if (item instanceof Map) lista.add(lerSerie((Map<?, ?>) item));
        return lista;
    }

    private Serie lerSerie(Map<?, ?> objeto) {
        ArrayList<String> generos = new ArrayList<String>();
        Object lista = objeto.get("generos");
        if (lista instanceof ArrayList) for (Object item : (ArrayList<?>) lista) if (item != null) generos.add(item.toString());
        return new Serie(inteiro(objeto.get("id"), -1), texto(objeto.get("nome")), texto(objeto.get("idioma")),
                generos, decimal(objeto.get("nota"), -1), texto(objeto.get("estado")),
                texto(objeto.get("dataEstreia")), texto(objeto.get("dataTermino")), texto(objeto.get("emissora")));
    }

    private ArrayList<Object> escreverLista(ArrayList<Serie> lista) {
        ArrayList<Object> resultado = new ArrayList<Object>();
        if (lista == null) return resultado;
        for (Serie serie : lista) {
            if (serie == null) continue;
            Map<String, Object> objeto = new LinkedHashMap<String, Object>();
            objeto.put("id", serie.getId()); objeto.put("nome", serie.getNome());
            objeto.put("idioma", serie.getIdioma()); objeto.put("generos", serie.getGeneros());
            objeto.put("nota", serie.getNota()); objeto.put("estado", serie.getEstado());
            objeto.put("dataEstreia", serie.getDataEstreia()); objeto.put("dataTermino", serie.getDataTermino());
            objeto.put("emissora", serie.getEmissora()); resultado.add(objeto);
        }
        return resultado;
    }

    private String texto(Object valor) { return valor == null ? "" : valor.toString(); }
    private int inteiro(Object valor, int padrao) { return valor instanceof Number ? ((Number) valor).intValue() : padrao; }
    private double decimal(Object valor, double padrao) { return valor instanceof Number ? ((Number) valor).doubleValue() : padrao; }
    public boolean isDadosPadraoUsados() { return dadosPadraoUsados; }
    public String getAvisoCarregamento() { return avisoCarregamento; }
    public Path getArquivo() { return arquivo.toAbsolutePath(); }

    private String formatarJson(String compacto) throws IOException {
        StringBuilder bonito = new StringBuilder();
        boolean string = false, escape = false;
        int nivel = 0;
        for (int i = 0; i < compacto.length(); i++) {
            char c = compacto.charAt(i);
            if (string) {
                bonito.append(c);
                if (escape) escape = false;
                else if (c == '\\') escape = true;
                else if (c == '"') string = false;
            } else if (c == '"') { string = true; bonito.append(c); }
            else if (c == '{' || c == '[') { bonito.append(c).append('\n'); nivel++; indentar(bonito, nivel); }
            else if (c == '}' || c == ']') { bonito.append('\n'); nivel--; indentar(bonito, nivel); bonito.append(c); }
            else if (c == ',') { bonito.append(c).append('\n'); indentar(bonito, nivel); }
            else if (c == ':') bonito.append(": ");
            else bonito.append(c);
        }
        return bonito.toString();
    }

    private void indentar(StringBuilder texto, int nivel) { for (int i = 0; i < nivel; i++) texto.append("  "); }
}
