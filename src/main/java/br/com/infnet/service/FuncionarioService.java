package br.com.infnet.service;

import br.com.infnet.model.Funcionario;
import br.com.infnet.repository.IRepository;
import java.util.List;
import java.util.Optional;

public class FuncionarioService {

    private static final int MAX_LENGTH = 50;
    private final IRepository<Funcionario> repository;

    public FuncionarioService(IRepository<Funcionario> repository) {
        this.repository = repository;
    }

    // --- Lógica de Validação com Cláusulas de Guarda (Requisito 1) ---
    private void validarFuncionario(String nome, String cargo) {

        // Normaliza entradas ANTES de validar
        if (nome != null) nome = nome.trim();
        if (cargo != null) cargo = cargo.trim();

        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        if (cargo == null || cargo.isEmpty()) {
            throw new IllegalArgumentException("Cargo é obrigatório.");
        }

        if (nome.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("O limite de " + MAX_LENGTH + " caracteres foi excedido para Nome.");
        }

        if (cargo.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("O limite de " + MAX_LENGTH + " caracteres foi excedido para Cargo.");
        }
    }

    // --- CRUD Operations ---

    public void addFuncionario(Funcionario funcionario) {
        validarFuncionario(funcionario.getNome(), funcionario.getCargo());
        repository.cadastrar(funcionario);
    }

    public Optional<Funcionario> findById(int id) {
        return Optional.ofNullable(repository.buscar(id));
    }

    public void updateFuncionario(Funcionario funcionario) {
        validarFuncionario(funcionario.getNome(), funcionario.getCargo());
        repository.atualizar(funcionario);
    }

    public void deleteFuncionario(int id) {
        repository.remover(id);
    }

    public List<Funcionario> listar(){
        return repository.listar();
    }
}


