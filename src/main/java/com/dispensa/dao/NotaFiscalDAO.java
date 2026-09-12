package com.dispensa.dao;

import com.dispensa.model.NotaFiscal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaFiscalDAO {

    public List<NotaFiscal> listarHistorico(int usuarioId) throws SQLException {
        List<NotaFiscal> lista = new ArrayList<>();

        String sql = "SELECT id, numero_nota, serie, chave_acesso, fornecedor, " +
                     "data_emissao, valor_total, status, origem " +
                     "FROM nota_fiscal WHERE usuario_id = ? ORDER BY data_emissao DESC";

        try (Connection con = ConexaoBD.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaFiscal nota = new NotaFiscal();
                    nota.setId(rs.getInt("id"));
                    nota.setNumeroNota(rs.getString("numero_nota"));
                    nota.setSerie(rs.getString("serie"));
                    nota.setChaveAcesso(rs.getString("chave_acesso"));
                    nota.setFornecedor(rs.getString("fornecedor"));

                    Date data = rs.getDate("data_emissao");
                    nota.setDataEmissao(data != null ? data.toString() : null);

                    nota.setValorTotal(rs.getDouble("valor_total"));
                    nota.setStatus(rs.getString("status"));
                    nota.setOrigem(rs.getString("origem"));

                    lista.add(nota);
                }
            }
        }
        return lista;
    }
}