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

        // LISTAGEM
        app.get("/funcionarios", ctx ->
                ctx.html(FuncionarioView.renderList(this.service.listar()))
        );

        // FORM CREATE
        app.get("/funcionarios/new", ctx -> {
            ctx.html(FuncionarioView.renderForm(new HashMap<>()));
        });

        // CREATE
        app.post("/funcionarios", ctx -> {
            String nome = ctx.formParam("nome");
            String cargo = ctx.formParam("cargo");
            double salario = 0.0;

            try {
                Funcionario novoFuncionario = new Funcionario(lastId++, nome, cargo, salario);
                this.service.addFuncionario(novoFuncionario);
                ctx.redirect("/funcionarios");

            } catch (IllegalArgumentException e) {
                Map<String, Object> model = new HashMap<>();
                model.put("error", e.getMessage());
                model.put("nome", nome);
                model.put("cargo", cargo);

                ctx.html(FuncionarioView.renderForm(model));
            }
        });

        // FORM EDIT
        app.get("/funcionarios/edit/{id}", ctx -> {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            Optional<Funcionario> funcionarioOptional = this.service.findById(id);

            if (funcionarioOptional.isPresent()) {
                Funcionario funcionario = funcionarioOptional.get();

                Map<String, Object> model = new HashMap<>();
                model.put("id", funcionario.getId());
                model.put("nome", funcionario.getNome());
                model.put("cargo", funcionario.getCargo());

                ctx.html(FuncionarioView.renderForm(model));
            } else {
                ctx.status(404).result("Funcionário não encontrado");
            }
        });

        // UPDATE
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
                Map<String, Object> model = new HashMap<>();
                model.put("id", id);
                model.put("error", e.getMessage());
                model.put("nome", nome);
                model.put("cargo", cargo);

                ctx.html(FuncionarioView.renderForm(model));
            }
        });

        // DELETE
        app.post("/funcionarios/delete/{id}", ctx -> {
            int id = ctx.pathParamAsClass("id", Integer.class).get();
            this.service.deleteFuncionario(id);
            ctx.redirect("/funcionarios");
        });
    }
}


