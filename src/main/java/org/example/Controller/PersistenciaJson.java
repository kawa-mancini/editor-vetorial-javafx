package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/**
 * Salva e carrega o desenho em arquivos JSON (classe extra, não está no diagrama).
 *
 * Formato do arquivo:
 * <pre>
 * {
 *   "versao": 1,
 *   "canvas": { "largura": 985.0, "altura": 770.0, "gridSnappingHabilitado": false, "tamanhoGrade": 20.0 },
 *   "formas": [
 *     { "tipo": "Circulo", "pontos": [ {"x": 100.0, "y": 80.0} ], "raio": 40.0,
 *       "corPreenchimentoHex": "#D3D3D3", "corBordaHex": "#000000", "espessuraBorda": 1.0,
 *       "groupId": null },
 *     ...
 *   ]
 * }
 * </pre>
 * A ordem do array "formas" é a ordem das camadas.
 */
public class PersistenciaJson {

    private static final int VERSAO_FORMATO = 1;

    // setPrettyPrinting: arquivo indentado, fácil de ler; serializeNulls: grava "groupId": null
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    /** Erro ao carregar, com uma mensagem em português pronta para mostrar ao usuário. */
    public static class ArquivoInvalidoException extends Exception {
        public ArquivoInvalidoException(String mensagem) {
            super(mensagem);
        }

        public ArquivoInvalidoException(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }
    }

    // ------------------------------------------------------------------
    // Salvar
    // ------------------------------------------------------------------

    public void salvar(Path arquivo, EditorContext contexto, double larguraCanvas, double alturaCanvas)
            throws IOException {
        JsonObject canvas = new JsonObject();
        canvas.addProperty("largura", larguraCanvas);
        canvas.addProperty("altura", alturaCanvas);
        canvas.addProperty("gridSnappingHabilitado", contexto.isGridSnappingHabilitado());
        canvas.addProperty("tamanhoGrade", EditorContext.TAMANHO_GRADE);

        JsonArray formas = new JsonArray();
        for (FormaGeometrica forma : contexto.getFormas()) {
            formas.add(formaParaJson(forma));
        }

        JsonObject raiz = new JsonObject();
        raiz.addProperty("versao", VERSAO_FORMATO);
        raiz.add("canvas", canvas);
        raiz.add("formas", formas);

        // Monta o texto inteiro antes de abrir o arquivo: se algo der errado,
        // o arquivo antigo não fica pela metade
        String texto = GSON.toJson(raiz);
        Files.writeString(arquivo, texto, StandardCharsets.UTF_8);
        contexto.marcarComoSalvo();
    }

    private JsonObject formaParaJson(FormaGeometrica forma) {
        JsonObject json = new JsonObject();
        // O nome da classe (Poligono, Circulo, Quadrado, Hexagono) vira o "tipo"
        json.addProperty("tipo", forma.getClass().getSimpleName());

        JsonArray pontos = new JsonArray();
        for (Ponto p : forma.getPontos()) {
            JsonObject ponto = new JsonObject();
            ponto.addProperty("x", p.getX());
            ponto.addProperty("y", p.getY());
            pontos.add(ponto);
        }
        json.add("pontos", pontos);

        if (forma instanceof Circulo) {
            json.addProperty("raio", ((Circulo) forma).getRaio());
        }

        json.addProperty("corPreenchimentoHex", forma.getCorPreenchimentoHex());
        json.addProperty("corBordaHex", forma.getCorBordaHex());
        json.addProperty("espessuraBorda", forma.getEspessuraBorda());
        json.addProperty("groupId", forma.getGroupId());
        return json;
    }

    // ------------------------------------------------------------------
    // Carregar
    // ------------------------------------------------------------------

    /**
     * Lê o arquivo e, SE tudo estiver certo, substitui o desenho do contexto.
     * Se houver qualquer problema, lança ArquivoInvalidoException explicando o
     * motivo e o desenho atual continua intacto.
     */
    public void carregar(Path arquivo, EditorContext contexto) throws ArquivoInvalidoException {
        JsonObject raiz = lerRaiz(arquivo);

        JsonElement formasJson = raiz.get("formas");
        if (formasJson == null || !formasJson.isJsonArray()) {
            throw new ArquivoInvalidoException("O arquivo não tem a lista de formas (campo \"formas\").");
        }

        // Primeiro monta TODAS as formas numa lista separada...
        List<FormaGeometrica> formas = new ArrayList<>();
        JsonArray array = formasJson.getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            try {
                formas.add(jsonParaForma(array.get(i)));
            } catch (ArquivoInvalidoException | IllegalArgumentException | IllegalStateException
                     | UnsupportedOperationException e) {
                // As validações dos Models (raio > 0, 3 pontos...) também caem aqui
                throw new ArquivoInvalidoException("Forma nº " + (i + 1) + " inválida: " + e.getMessage(), e);
            }
        }
        boolean gridSnapping = lerGridSnapping(raiz);

        // ...e só depois que tudo deu certo mexe no contexto
        contexto.setFormas(formas);
        contexto.setFormaEmAndamento(null);
        contexto.setGridSnappingHabilitado(gridSnapping);
        contexto.getHistorico().limpar(); // o desfazer não deve voltar para o desenho anterior
        contexto.marcarComoSalvo();
    }

    private JsonObject lerRaiz(Path arquivo) throws ArquivoInvalidoException {
        String conteudo;
        try {
            conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            throw new ArquivoInvalidoException("O arquivo não é um texto UTF-8 (talvez não seja um arquivo do editor).", e);
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo: " + e.getMessage(), e);
        }

        JsonElement elemento;
        try {
            elemento = JsonParser.parseString(conteudo);
        } catch (JsonParseException e) {
            throw new ArquivoInvalidoException("O arquivo não é um JSON válido (está corrompido ou incompleto). "
                    + "Detalhe técnico: " + e.getMessage(), e);
        }
        if (!elemento.isJsonObject()) {
            throw new ArquivoInvalidoException("O arquivo está vazio ou não contém um desenho do editor.");
        }
        return elemento.getAsJsonObject();
    }

    /** Monta a forma certa a partir do campo "tipo". */
    private FormaGeometrica jsonParaForma(JsonElement elemento) throws ArquivoInvalidoException {
        if (!elemento.isJsonObject()) {
            throw new ArquivoInvalidoException("cada forma deveria ser um objeto JSON.");
        }
        JsonObject json = elemento.getAsJsonObject();
        String tipo = lerTexto(json, "tipo");
        List<Ponto> pontos = lerPontos(json);

        // Os construtores conferem as regras de negócio e lançam IllegalArgumentException
        FormaGeometrica forma = switch (tipo) {
            case "Poligono" -> new Poligono(pontos);
            case "Quadrado" -> Quadrado.aPartirDosVertices(pontos);
            case "Hexagono" -> Hexagono.aPartirDosVertices(pontos);
            case "Circulo" -> {
                if (pontos.size() != 1) {
                    throw new ArquivoInvalidoException("o círculo deveria ter só o ponto central.");
                }
                yield new Circulo(pontos.get(0), lerNumero(json, "raio"));
            }
            default -> throw new ArquivoInvalidoException("tipo de forma desconhecido: \"" + tipo + "\".");
        };

        forma.setCorPreenchimentoHex(lerTexto(json, "corPreenchimentoHex"));
        forma.setCorBordaHex(lerTexto(json, "corBordaHex"));
        forma.setEspessuraBorda(lerNumero(json, "espessuraBorda"));

        JsonElement groupId = json.get("groupId");
        if (groupId != null && !groupId.isJsonNull()) {
            forma.setGroupId(groupId.getAsString());
        }
        return forma;
    }

    private List<Ponto> lerPontos(JsonObject json) throws ArquivoInvalidoException {
        JsonElement elemento = json.get("pontos");
        if (elemento == null || !elemento.isJsonArray()) {
            throw new ArquivoInvalidoException("falta a lista de pontos (campo \"pontos\").");
        }
        List<Ponto> pontos = new ArrayList<>();
        for (JsonElement p : elemento.getAsJsonArray()) {
            if (!p.isJsonObject()) {
                throw new ArquivoInvalidoException("cada ponto deveria ter \"x\" e \"y\".");
            }
            pontos.add(new Ponto(lerNumero(p.getAsJsonObject(), "x"), lerNumero(p.getAsJsonObject(), "y")));
        }
        return pontos;
    }

    private boolean lerGridSnapping(JsonObject raiz) throws ArquivoInvalidoException {
        JsonElement canvas = raiz.get("canvas");
        if (canvas == null || !canvas.isJsonObject()) {
            return false; // configurações do canvas são opcionais
        }
        JsonElement grid = canvas.getAsJsonObject().get("gridSnappingHabilitado");
        if (grid == null) {
            return false;
        }
        if (!grid.isJsonPrimitive() || !grid.getAsJsonPrimitive().isBoolean()) {
            throw new ArquivoInvalidoException("\"gridSnappingHabilitado\" deveria ser true ou false.");
        }
        return grid.getAsBoolean();
    }

    private String lerTexto(JsonObject json, String campo) throws ArquivoInvalidoException {
        JsonElement valor = json.get(campo);
        if (valor == null || !valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isString()) {
            throw new ArquivoInvalidoException("o campo \"" + campo + "\" está faltando ou não é um texto.");
        }
        return valor.getAsString();
    }

    private double lerNumero(JsonObject json, String campo) throws ArquivoInvalidoException {
        JsonElement valor = json.get(campo);
        if (valor == null || !valor.isJsonPrimitive() || !valor.getAsJsonPrimitive().isNumber()) {
            throw new ArquivoInvalidoException("o campo \"" + campo + "\" está faltando ou não é um número.");
        }
        double numero = valor.getAsDouble();
        if (!Double.isFinite(numero)) {
            throw new ArquivoInvalidoException("o campo \"" + campo + "\" tem um valor numérico inválido.");
        }
        return numero;
    }
}
