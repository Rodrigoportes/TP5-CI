package br.com.infnet.controller;

import br.com.infnet.model.Funcionario;
import br.com.infnet.service.FuncionarioService;
import br.com.infnet.view.FuncionarioView;
import io.javalin.Javalin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FuncionarioController {

    private final FuncionarioService service;
    private int lastId = 1000;

    public FuncionarioController(Javalin app, FuncionarioService service) {
        this.service = service;

        // LISTAGEM (GET /funcionarios)
        app.get("/funcionarios", ctx ->
                ctx.html(FuncionarioView.renderList(this.service.listar()))
        );

        // FORM CREATE (GET /funcionarios/new)
        app.get("/funcionarios/new", ctx -> {
            ctx.html(FuncionarioView.renderForm(new HashMap<>()));
        });

        // CREATE (POST /funcionarios)
        app.post("/funcionarios", ctx -> {
            String nome = ctx.formParam("nome");
            String cargo = ctx.formParam("cargo");
            double salario = 0.0;

            try {
                // 1. Cria objeto e delega validação ao Service
                Funcionario novoFuncionario = new Funcionario(lastId++, nome, cargo, salario);
                this.service.addFuncionario(novoFuncionario);

                // 2. Sucesso: Redireciona
                ctx.redirect("/funcionarios");

            } catch (IllegalArgumentException e) {
                // 3. FALHA DE VALIDAÇÃO: Retorna 400 e a mensagem no corpo (Coerente)
                ctx.status(400);
                ctx.result(e.getMessage());
            }
        });

        // FORM EDIT (GET /funcionarios/edit/{id})
        app.get("/funcionarios/edit/{id}", ctx -> {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            Optional<Funcionario> funcionarioOptional = this.service.findById(id);

            if (funcionarioOptional.isPresent()) {
                Funcionario funcionario = funcionarioOptional.get();

                Map<String, Object> model = new HashMap<>();
                model.put("id", funcionario.getId());
                model.put("nome", funcionario.getNome());
                model.put("cargo", funcionario.getCargo());
                model.put("salario", funcionario.getSalario());

                ctx.html(FuncionarioView.renderForm(model));
            } else {
                ctx.status(404).result("Funcionário não encontrado");
            }
        });

        // UPDATE (POST /funcionarios/edit/{id}) - FIX E2E COERÊNCIA
        app.post("/funcionarios/edit/{id}", ctx -> {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            String nome = ctx.formParam("nome");
            String cargo = ctx.formParam("cargo");

            double salario = this.service.findById(id).map(Funcionario::getSalario).orElse(0.0);

            try {
                Funcionario atualizado = new Funcionario(id, nome, cargo, salario);
                this.service.updateFuncionario(atualizado);
                ctx.redirect("/funcionarios");

            } catch (IllegalArgumentException e) {
                // CORREÇÃO FINAL: Retorna 400 Bad Request com a mensagem no corpo.
                ctx.status(400);
                ctx.result(e.getMessage());
            }
        });

        // DELETE (POST /funcionarios/delete/{id})
        app.post("/funcionarios/delete/{id}", ctx -> {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            this.service.deleteFuncionario(id);
            ctx.redirect("/funcionarios");
        });
    }
}



