import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class TvMazeServico {
    private final JsonSimples json = new JsonSimples();

    public ArrayList<Serie> buscarSeries(String nome) throws Exception {
        String pesquisa = nome == null ? "" : nome.trim();
        if (pesquisa.isEmpty()) throw new Exception("Digite o nome de uma serie.");
        if (pesquisa.length() > 150) throw new Exception("O texto da pesquisa e muito grande.");

        String endereco = "https://api.tvmaze.com/search/shows?q="
                + URLEncoder.encode(pesquisa, StandardCharsets.UTF_8);
        HttpURLConnection conexao = null;
        try {
            conexao = (HttpURLConnection) new URL(endereco).openConnection();
            conexao.setRequestMethod("GET");
            conexao.setConnectTimeout(8000);
            conexao.setReadTimeout(10000);
            conexao.setRequestProperty("Accept", "application/json");
            int resposta = conexao.getResponseCode();
            if (resposta != 200) throw new Exception("O TVMaze respondeu com o codigo HTTP " + resposta + ".");
            String texto = lerResposta(conexao.getInputStream());
            Object raiz = json.ler(texto);
            if (!(raiz instanceof ArrayList)) throw new Exception("A resposta do TVMaze estava invalida.");
            return converterResultados((ArrayList<?>) raiz);
        } catch (java.net.UnknownHostException e) {
            throw new Exception("Nao foi possivel conectar ao TVMaze. Verifique sua internet.");
        } catch (java.net.SocketTimeoutException e) {
            throw new Exception("O TVMaze demorou para responder. Tente novamente.");
        } catch (java.io.IOException e) {
            throw new Exception("Nao foi possivel conectar ao TVMaze. Verifique sua internet.");
        } finally {
            if (conexao != null) conexao.disconnect();
        }
    }

    private String lerResposta(InputStream entrada) throws Exception {
        BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        StringBuilder resposta = new StringBuilder();
        String linha;
        while ((linha = leitor.readLine()) != null) resposta.append(linha);
        leitor.close();
        if (resposta.toString().trim().isEmpty()) throw new Exception("O TVMaze enviou uma resposta vazia.");
        return resposta.toString();
    }

    private ArrayList<Serie> converterResultados(ArrayList<?> resultados) {
        ArrayList<Serie> series = new ArrayList<Serie>();
        for (Object item : resultados) {
            if (!(item instanceof Map)) continue;
            Object show = ((Map<?, ?>) item).get("show");
            if (show instanceof Map) series.add(converterSerie((Map<?, ?>) show));
        }
        return series;
    }

    private Serie converterSerie(Map<?, ?> show) {
        int id = numeroInteiro(show.get("id"), -1);
        String nome = texto(show.get("name"));
        String idioma = texto(show.get("language"));
        ArrayList<String> generos = new ArrayList<String>();
        Object listaGeneros = show.get("genres");
        if (listaGeneros instanceof ArrayList) {
            for (Object genero : (ArrayList<?>) listaGeneros) if (genero != null) generos.add(genero.toString());
        }
        double nota = -1;
        Object avaliacao = show.get("rating");
        if (avaliacao instanceof Map) nota = numeroDouble(((Map<?, ?>) avaliacao).get("average"), -1);
        String emissora = "";
        Object network = show.get("network");
        if (!(network instanceof Map)) network = show.get("webChannel");
        if (network instanceof Map) emissora = texto(((Map<?, ?>) network).get("name"));
        return new Serie(id, nome, idioma, generos, nota, texto(show.get("status")),
                texto(show.get("premiered")), texto(show.get("ended")), emissora);
    }

    private String texto(Object valor) {
        if (valor == null) {
            return "";
        }
        return valor.toString();
    }

    private int numeroInteiro(Object valor, int padrao) {
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        return padrao;
    }

    private double numeroDouble(Object valor, double padrao) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return padrao;
    }
}
