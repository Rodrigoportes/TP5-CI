package br.com.infnet.tests;

import br.com.infnet.Main;
import br.com.infnet.pages.FuncionarioFormPage;
import br.com.infnet.pages.FuncionarioListPage;
import br.com.infnet.service.FuncionarioService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FuncionarioWebTest {

    private static final String BASE_URL = "http://localhost:7000";
    private WebDriver driver;
    private io.javalin.Javalin app;
    private FuncionarioListPage listPage;
    private FuncionarioFormPage formPage;

    // Cada teste terá um driver e servidor NOVOS
    @BeforeEach
    public void setup() {
        // ---- INICIA O SERVIDOR ----
        app = Main.startApp(7000);

        // ---- INICIALIZA O CHROME ----
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        // ---- PAGE OBJECTS ----
        listPage = new FuncionarioListPage(driver);
        formPage = new FuncionarioFormPage(driver);
    }

    // PARA CADA TESTE um novo servidor E um novo navegador são limpos
    @AfterEach
    public void teardown() {
        if (driver != null) driver.quit();
        if (app != null) app.stop();
    }

    // CREATE
    @Order(1)
    @ParameterizedTest(name = "Cadastro: {0} - {1}")
    @CsvSource({
            "Pedro Alvares, Gerente de Projetos",
            "Ana Carolina, Desenvolvedora Júnior"
    })
    public void testCreateNewFuncionario(String nome, String cargo) {
        listPage.navigateToList();
        listPage.clickNewFuncionario();
        formPage.fillForm(nome, cargo);
        formPage.submitForm();

        assertTrue(listPage.isFuncionarioPresent(nome, cargo),
                "O novo funcionário não foi encontrado na lista após o cadastro.");
    }

    // UPDATE
    @Test
    @Order(2)
    public void testUpdateFuncionario() {
        String nomeAntigo = "João Silva";
        String novoCargo = "Tech Lead Sênior";

        listPage.navigateToList();
        listPage.clickEdit(nomeAntigo);

        formPage.fillForm(nomeAntigo, novoCargo);
        formPage.submitForm();

        assertTrue(listPage.isFuncionarioPresent(nomeAntigo, novoCargo),
                "O cargo do funcionário não foi atualizado na lista.");
    }

    // DELETE
    @Test
    @Order(3)
    public void testDeleteFuncionario() {
        String nomeParaDeletar = "Clara Oliveira";

        listPage.navigateToList();
        listPage.deleteFuncionario(nomeParaDeletar);

        assertTrue(!driver.getPageSource().contains(nomeParaDeletar),
                "O funcionário não foi deletado da lista.");
    }

    // CAMPOS VAZIOS
    @Test
    @Order(4)
    @DisplayName("Teste Negativo: Bloqueio de Formulário com Campos Vazios")
    public void testNegativeCreateEmptyFields() {
        listPage.navigateToList();
        listPage.clickNewFuncionario();

        formPage.fillForm("Teste Vazio", "");

        formPage.clickSubmitButton();

        assertTrue(driver.getCurrentUrl().contains("/funcionarios/new"),
                "O navegador aceitou submissão com campo vazio.");
    }

    // 404
    @Test
    @Order(5)
    @DisplayName("Teste Negativo: Edição de Funcionário Inexistente (404)")
    public void testNegativeEditNonExistentFuncionario() {
        driver.get(BASE_URL + "/funcionarios/edit/999");

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Funcionário não encontrado")
                        || !pageSource.contains("Editar Item"),
                "O erro 404 não foi exibido corretamente.");
    }

    @Test
    @Order(6)
    @DisplayName("Teste de Segurança: Fuzzing e Entrada Maliciosa (Limite de Caracteres) - FINAL")
    public void testFuzzingExceedsMaxLength() {
        // A substring mais robusta, focando apenas na prova do limite excedido.
        final String expectedErrorMessageSubstring = FuncionarioService.MAX_LENGTH + " caracteres foi excedido";

        // Criamos o input para exceder o limite em 1 caractere
        String tooLongInput = "A".repeat(FuncionarioService.MAX_LENGTH + 1);

        listPage.navigateToList();
        listPage.clickNewFuncionario(); // Vai para o formulário

        formPage.fillForm(tooLongInput, "Avaliador");

        // Submete a forma, esperando 400 Bad Request
        formPage.clickSubmitButton();

        // 1. Verifica se a MENSAGEM de erro está presente no corpo da resposta 400
        String pageSource = driver.getPageSource();

        // A asserção agora procura apenas pelo número e o termo "excedido"
        assertTrue(pageSource.contains(expectedErrorMessageSubstring),
                "A validação falhou; a substring de erro ('" + expectedErrorMessageSubstring + "') não foi encontrada no corpo da resposta 400.");

    }




}
