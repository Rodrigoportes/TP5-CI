package br.com.infnet.model;

public class Funcionario implements Identifiable {

    private final int id;
    private final String nome;
    private final String cargo;
    private final double salario;

    public Funcionario(int id, String nome, String cargo, double salario) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.salario = salario;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }

    public double getSalario() {
        return salario;
    }

    @Override
    public String toString() {
        return "Funcionario{id=" + id + ", nome='" + nome + "', cargo='" + cargo + "', salario=" + salario + "}";
    }
}
