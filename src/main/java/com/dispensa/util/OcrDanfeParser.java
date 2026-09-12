package com.dispensa.util;

import com.dispensa.model.OcrSpaceResponse;
import com.dispensa.model.OcrSugestao;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OcrDanfeParser {

    private static final String OCR_SPACE_URL = "https://api.ocr.space/parse/image";
    private static final String API_KEY = "K82171363888957";
    private static final Gson gson = new Gson();

    public static OcrSugestao processarImagem(byte[] imagemBytes, String nomeArquivo) throws Exception {

        String boundary = "----DispensaBoundary" + System.currentTimeMillis();
        byte[] corpoMultipart = montarCorpoMultipart(boundary, imagemBytes, nomeArquivo);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OCR_SPACE_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpoMultipart))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        OcrSpaceResponse resultado = gson.fromJson(response.body(), OcrSpaceResponse.class);

        if (resultado.IsErroredOnProcessing || resultado.ParsedResults == null || resultado.ParsedResults.isEmpty()) {
            throw new Exception("Falha no OCR: " + resultado.ErrorMessage);
        }

        String texto = resultado.ParsedResults.get(0).ParsedText;
        return extrairSugestoes(texto);
    }

    private static byte[] montarCorpoMultipart(String boundary, byte[] imagemBytes, String nomeArquivo) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String quebraLinha = "\r\n";

        out.write(("--" + boundary + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"apikey\"" + quebraLinha + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write((API_KEY + quebraLinha).getBytes(StandardCharsets.UTF_8));

        out.write(("--" + boundary + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"language\"" + quebraLinha + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(("por" + quebraLinha).getBytes(StandardCharsets.UTF_8));

        out.write(("--" + boundary + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + nomeArquivo + "\"" + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: application/octet-stream" + quebraLinha + quebraLinha).getBytes(StandardCharsets.UTF_8));
        out.write(imagemBytes);
        out.write(quebraLinha.getBytes(StandardCharsets.UTF_8));

        out.write(("--" + boundary + "--" + quebraLinha).getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }

    private static OcrSugestao extrairSugestoes(String texto) {
    OcrSugestao sugestao = new OcrSugestao();
    sugestao.setTextoBruto(texto);

    String[] linhas = texto.split("\\r?\\n");
    sugestao.setFornecedorSugerido(extrairFornecedor(linhas));

    Pattern padraoValor = Pattern.compile("(?i)valor\\s*total[^0-9]{0,15}([0-9][0-9.,\\s]{1,15}[0-9])");
    Matcher matcherValor = padraoValor.matcher(texto);
    if (matcherValor.find()) {
        sugestao.setValorSugerido(normalizarValorMonetario(matcherValor.group(1)));
    }

    Pattern padraoData = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})");
    Matcher matcherData = padraoData.matcher(texto);
    if (matcherData.find()) {
        sugestao.setDataSugerida(matcherData.group(3) + "-" + matcherData.group(2) + "-" + matcherData.group(1));
    }

    // Tenta capturar algo como "NFCe 143.199 Serie 002" ou "NFCe 143 199 Série 002"
    Pattern padraoNota = Pattern.compile("(?i)NFCe\\s*([\\d.,\\s]{3,15})\\s*S[ée]rie\\s*(\\d+)");
    Matcher matcherNota = padraoNota.matcher(texto);
    if (matcherNota.find()) {
    String numero = matcherNota.group(1).replaceAll("[.,\\s]", "");
    sugestao.setNumeroNotaSugerido(numero);
    sugestao.setSerieSugerida(matcherNota.group(2));
}

    sugestao.setItensSugeridos(extrairItens(texto)); // <-- novo

    return sugestao;
    }

// Tenta pegar o nome do estabelecimento, ignorando o número do CNPJ que costuma vir junto
private static String extrairFornecedor(String[] linhas) {
    for (int i = 0; i < linhas.length; i++) {
        String linha = linhas[i].trim();
        if (linha.toUpperCase().contains("CNPJ")) {
            String resto = linha.replaceAll("(?i).*CNPJ[:\\s]*[\\d.,/-]+", "").trim();

            // Junta a próxima linha também, caso o nome tenha quebrado em duas linhas
            if (i + 1 < linhas.length) {
                String proxima = linhas[i + 1].trim();
                boolean pareceEndereco = proxima.matches("(?i)^(AV|RUA|R\\.|TRAVESSA|ALAMEDA).*")
                        || proxima.matches(".*\\d{3,}.*");
                if (!pareceEndereco && !proxima.isBlank()) {
                    resto = resto + " " + proxima;
                }
            }
            return resto.trim();
        }
    }
    for (String linha : linhas) {
        if (!linha.isBlank()) return linha.trim();
    }
    return null;
}

// Limpa valores tipo "1, 615, 42" ou "1.615,42" e transforma em 1615.42
private static double normalizarValorMonetario(String bruto) {
    String limpo = bruto.replaceAll("\\s+", "");

    int ultimaVirgula = limpo.lastIndexOf(',');
    int ultimoPonto = limpo.lastIndexOf('.');
    int ultimoSeparador = Math.max(ultimaVirgula, ultimoPonto);

    if (ultimoSeparador == -1) {
        return Double.parseDouble(limpo);
    }

    String parteInteira = limpo.substring(0, ultimoSeparador).replaceAll("[.,]", "");
    String parteDecimal = limpo.substring(ultimoSeparador + 1);

    if (parteDecimal.length() != 2) {
        return Double.parseDouble(limpo.replaceAll("[.,]", ""));
    }

    return Double.parseDouble(parteInteira + "." + parteDecimal);
}

private static List<com.dispensa.model.ItemOcrSugestao> extrairItens(String texto) {
    List<com.dispensa.model.ItemOcrSugestao> itens = new ArrayList<>();
    String[] linhas = texto.split("\\r?\\n");

    int inicio = -1;
    int fim = linhas.length;

    for (int i = 0; i < linhas.length; i++) {
        String l = linhas[i].toLowerCase();
        if (inicio == -1 && (l.contains("cod") || l.contains("descr") || l.contains("escr"))) {
            inicio = i + 1;
        }
        if (inicio != -1 && (l.contains("total de it") || l.contains("valor total") || l.contains("qtd total") || l.contains("qtd. total"))) {
            fim = i;
            break;
        }
    }

    if (inicio == -1) inicio = 0;

    StringBuilder buffer = new StringBuilder();

    for (int i = inicio; i < fim; i++) {
        String linha = linhas[i].trim();
        if (linha.isEmpty()) continue;

        buffer.append(" ").append(linha);

        Matcher numeros = Pattern.compile("\\d+[.,]?\\d*").matcher(buffer);
        List<String> valoresEncontrados = new ArrayList<>();
        while (numeros.find()) valoresEncontrados.add(numeros.group());

        if (valoresEncontrados.size() >= 3) {
            com.dispensa.model.ItemOcrSugestao item = interpretarBufferItem(buffer.toString(), valoresEncontrados);
            if (item != null) itens.add(item);
            buffer.setLength(0);
        }
    }

    return itens;
}

private static com.dispensa.model.ItemOcrSugestao interpretarBufferItem(String bufferTexto, List<String> numeros) {
    String nome = bufferTexto.replaceAll("\\d+[.,]?\\d*", " ")
                             .replaceAll("[^\\p{L}\\s]", " ")
                             .trim()
                             .replaceAll("\\s+", " ");

    if (nome.isBlank() || numeros.isEmpty()) return null;

    String ultimoValor = numeros.get(numeros.size() - 1);
    double valor;
    try {
        valor = normalizarValorMonetario(ultimoValor);
    } catch (Exception e) {
        return null;
    }

    com.dispensa.model.ItemOcrSugestao item = new com.dispensa.model.ItemOcrSugestao();
    item.setNomeSugerido(nome);
    item.setValorSugerido(valor);
    return item;
}

}
