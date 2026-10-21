package org.example.Controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.example.Controller.PersistenciaJson.ArquivoInvalidoException;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Testes de salvar/carregar JSON e do tratamento de arquivos corrompidos. */
class PersistenciaJsonTest {

    private static final double DELTA = 1e-9;

    // O JUnit cria uma pasta temporária para cada teste e apaga no final
    @TempDir
    Path pasta;

    private PersistenciaJson persistencia;
    private EditorContext contexto;

    @BeforeEach
    void preparar() {
        persistencia = new PersistenciaJson();
        contexto = new EditorContext();
    }

    private Path escrever(String nome, String conteudo) throws IOException {
        Path arquivo = pasta.resolve(nome);
        Files.writeString(arquivo, conteudo, StandardCharsets.UTF_8);
        return arquivo;
    }

    // ---------------- Ida e volta ----------------

    @Test
    void salvarECarregarMantemTodasAsFormas() throws Exception {
        Poligono triangulo = new Poligono(List.of(new Ponto(0, 0), new Ponto(40, 0), new Ponto(0, 30)));
        triangulo.setCorPreenchimentoHex("#FF0000");
        triangulo.setCorBordaHex("#0000FF");
        triangulo.setEspessuraBorda(4);
        Circulo circulo = new Circulo(new Ponto(200, 100), 35);
        circulo.setGroupId("grupo-1");
        Quadrado quadrado = new Quadrado(new Ponto(300, 300), 50);
        quadrado.setGroupId("grupo-1");
        Hexagono hexagono = new Hexagono(new Ponto(500, 300), 40);

        contexto.adicionarForma(triangulo);
        contexto.adicionarForma(circulo);
        contexto.adicionarForma(quadrado);
        contexto.adicionarForma(hexagono);
        contexto.setGridSnappingHabilitado(true);

        Path arquivo = pasta.resolve("desenho.json");
        persistencia.salvar(arquivo, contexto, 800, 600);

        EditorContext outro = new EditorContext();
        persistencia.carregar(arquivo, outro);

        List<FormaGeometrica> formas = outro.getFormas();
        assertEquals(4, formas.size());
        // Mesmos tipos e na mesma ordem (camadas)
        assertInstanceOf(Poligono.class, formas.get(0));
        assertInstanceOf(Circulo.class, formas.get(1));
        assertInstanceOf(Quadrado.class, formas.get(2));
        assertInstanceOf(Hexagono.class, formas.get(3));

        assertEquals("#FF0000", formas.get(0).getCorPreenchimentoHex());
        assertEquals("#0000FF", formas.get(0).getCorBordaHex());
        assertEquals(4, formas.get(0).getEspessuraBorda(), DELTA);
        assertEquals(600, formas.get(0).calcularArea(), DELTA);
        assertEquals(35, ((Circulo) formas.get(1)).getRaio(), DELTA);
        assertEquals("grupo-1", formas.get(1).getGroupId());
        assertEquals("grupo-1", formas.get(2).getGroupId());
        assertNull(formas.get(3).getGroupId());
        assertEquals(hexagono.calcularArea(), formas.get(3).calcularArea(), DELTA);
        assertTrue(outro.isGridSnappingHabilitado());
    }

    @Test
    void quadradoTransformadoVoltaComOsMesmosVertices() throws Exception {
        Quadrado quadrado = new Quadrado(new Ponto(100, 100), 50);
        quadrado.rotacionar(0.4);
        quadrado.cisalhar(0.3, 0);
        contexto.adicionarForma(quadrado);

        Path arquivo = pasta.resolve("q.json");
        persistencia.salvar(arquivo, contexto, 800, 600);
        EditorContext outro = new EditorContext();
        persistencia.carregar(arquivo, outro);

        FormaGeometrica carregado = outro.getFormas().get(0);
        for (int i = 0; i < 4; i++) {
            assertEquals(quadrado.getPontos().get(i).getX(), carregado.getPontos().get(i).getX(), DELTA);
            assertEquals(quadrado.getPontos().get(i).getY(), carregado.getPontos().get(i).getY(), DELTA);
        }
    }

    @Test
    void salvarECarregarMarcamComoSalvoECarregarLimpaOHistorico() throws Exception {
        contexto.registrarEstado();
        contexto.adicionarForma(new Circulo(new Ponto(0, 0), 5));
        assertTrue(contexto.temAlteracoesNaoSalvas());

        Path arquivo = pasta.resolve("d.json");
        persistencia.salvar(arquivo, contexto, 800, 600);
        assertFalse(contexto.temAlteracoesNaoSalvas());

        persistencia.carregar(arquivo, contexto);
        assertFalse(contexto.getHistorico().podeDesfazer());
        assertFalse(contexto.temAlteracoesNaoSalvas());
    }

    @Test
    void arquivoSalvoEhJsonLegivelComOsCamposPedidos() throws Exception {
        contexto.adicionarForma(new Circulo(new Ponto(10, 20), 5));
        Path arquivo = pasta.resolve("d.json");
        persistencia.salvar(arquivo, contexto, 800, 600);

        String texto = Files.readString(arquivo);
        assertTrue(texto.contains("\"tipo\": \"Circulo\""));
        assertTrue(texto.contains("\"raio\": 5.0"));
        assertTrue(texto.contains("\"groupId\": null"));
        assertTrue(texto.contains("\"canvas\""));
    }

    @Test
    void arquivosDaPastaExemplos() throws Exception {
        // O Maven roda os testes a partir da pasta do projeto
        persistencia.carregar(Path.of("exemplos", "desenho-exemplo.json"), contexto);
        assertEquals(4, contexto.getFormas().size());
        assertEquals(contexto.getFormas().get(1).getGroupId(), contexto.getFormas().get(2).getGroupId());

        assertThrows(ArquivoInvalidoException.class,
                () -> persistencia.carregar(Path.of("exemplos", "arquivo-corrompido.json"), contexto));
    }

    // ---------------- Arquivos corrompidos ----------------

    /** Carrega um arquivo inválido e confere que o desenho atual não mudou. */
    private ArquivoInvalidoException carregarInvalido(String conteudo) throws IOException {
        Circulo existente = new Circulo(new Ponto(1, 1), 1);
        contexto.adicionarForma(existente);
        Path arquivo = escrever("ruim.json", conteudo);

        ArquivoInvalidoException erro = assertThrows(ArquivoInvalidoException.class,
                () -> persistencia.carregar(arquivo, contexto));
        assertEquals(List.of(existente), contexto.getFormas(), "o desenho atual não pode ser alterado");
        assertFalse(erro.getMessage().isBlank());
        return erro;
    }

    @Test
    void jsonQuebrado() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido("{ \"formas\": [ { \"tipo\": \"Circulo\", ");
        assertTrue(erro.getMessage().contains("não é um JSON válido"));
    }

    @Test
    void arquivoVazio() throws IOException {
        carregarInvalido("");
    }

    @Test
    void semListaDeFormas() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido("{ \"versao\": 1 }");
        assertTrue(erro.getMessage().contains("formas"));
    }

    @Test
    void tipoDesconhecido() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido(
                "{ \"formas\": [ { \"tipo\": \"Estrela\", \"pontos\": [] } ] }");
        assertTrue(erro.getMessage().contains("Estrela"));
    }

    @Test
    void circuloComRaioZero() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido("{ \"formas\": [ { \"tipo\": \"Circulo\", "
                + "\"pontos\": [ {\"x\": 1, \"y\": 1} ], \"raio\": 0, \"corPreenchimentoHex\": \"#FFFFFF\", "
                + "\"corBordaHex\": \"#000000\", \"espessuraBorda\": 1, \"groupId\": null } ] }");
        assertTrue(erro.getMessage().contains("raio"));
    }

    @Test
    void poligonoComDoisPontos() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido("{ \"formas\": [ { \"tipo\": \"Poligono\", "
                + "\"pontos\": [ {\"x\": 1, \"y\": 1}, {\"x\": 2, \"y\": 2} ], \"corPreenchimentoHex\": \"#FFFFFF\", "
                + "\"corBordaHex\": \"#000000\", \"espessuraBorda\": 1 } ] }");
        assertTrue(erro.getMessage().contains("3 pontos"));
    }

    @Test
    void quadradoComNumeroErradoDeVertices() throws IOException {
        carregarInvalido("{ \"formas\": [ { \"tipo\": \"Quadrado\", \"pontos\": [ {\"x\": 1, \"y\": 1}, "
                + "{\"x\": 2, \"y\": 2}, {\"x\": 3, \"y\": 1} ], \"corPreenchimentoHex\": \"#FFFFFF\", "
                + "\"corBordaHex\": \"#000000\", \"espessuraBorda\": 1 } ] }");
    }

    @Test
    void corInvalida() throws IOException {
        ArquivoInvalidoException erro = carregarInvalido("{ \"formas\": [ { \"tipo\": \"Circulo\", "
                + "\"pontos\": [ {\"x\": 1, \"y\": 1} ], \"raio\": 3, \"corPreenchimentoHex\": \"azul\", "
                + "\"corBordaHex\": \"#000000\", \"espessuraBorda\": 1 } ] }");
        assertTrue(erro.getMessage().contains("azul"));
    }

    @Test
    void coordenadaQueNaoEhNumero() throws IOException {
        carregarInvalido("{ \"formas\": [ { \"tipo\": \"Circulo\", \"pontos\": [ {\"x\": \"dez\", \"y\": 1} ], "
                + "\"raio\": 3, \"corPreenchimentoHex\": \"#FFFFFF\", \"corBordaHex\": \"#000000\", "
                + "\"espessuraBorda\": 1 } ] }");
    }

    @Test
    void mensagemIndicaQualFormaTemProblema() throws IOException {
        String formaBoa = "{ \"tipo\": \"Circulo\", \"pontos\": [ {\"x\": 1, \"y\": 1} ], \"raio\": 3, "
                + "\"corPreenchimentoHex\": \"#FFFFFF\", \"corBordaHex\": \"#000000\", \"espessuraBorda\": 1 }";
        String formaRuim = "{ \"tipo\": \"Circulo\", \"pontos\": [ {\"x\": 1, \"y\": 1} ], \"raio\": -2, "
                + "\"corPreenchimentoHex\": \"#FFFFFF\", \"corBordaHex\": \"#000000\", \"espessuraBorda\": 1 }";
        ArquivoInvalidoException erro = carregarInvalido(
                "{ \"formas\": [ " + formaBoa + ", " + formaBoa + ", " + formaRuim + " ] }");
        assertTrue(erro.getMessage().contains("nº 3"));
    }

    @Test
    void arquivoQueNaoEhTexto() throws IOException {
        Path arquivo = pasta.resolve("imagem.json");
        Files.write(arquivo, new byte[] {(byte) 0x89, 'P', 'N', 'G', (byte) 0xFF, (byte) 0xFE, 0, 1});
        assertThrows(ArquivoInvalidoException.class, () -> persistencia.carregar(arquivo, contexto));
    }

    @Test
    void arquivoQueNaoExiste() {
        assertThrows(ArquivoInvalidoException.class,
                () -> persistencia.carregar(pasta.resolve("nao-existe.json"), contexto));
    }
}
