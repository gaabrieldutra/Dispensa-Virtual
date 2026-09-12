package com.dispensa.dao;

import java.sql.Connection;

public class TesteConexao {
    public static void main(String[] args) {
        try (Connection con = ConexaoBD.getConnection()) {
            System.out.println("Conectado com sucesso!");
        } catch (Exception e) {
            System.out.println("Falha na conexão:");
            e.printStackTrace();
        }
    }
}