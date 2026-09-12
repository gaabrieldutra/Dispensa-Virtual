package com.dispensa.controller;

import com.dispensa.dao.RelatorioDAO;
import com.dispensa.model.PrecoMedioResponse;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/api/produtos/preco-medio")
public class PrecoMedioServlet extends HttpServlet {

    private final RelatorioDAO dao = new RelatorioDAO();
    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            int usuarioId = Integer.parseInt(req.getParameter("usuarioId"));
            String nomeProduto = req.getParameter("nomeProduto");

            if (nomeProduto == null || nomeProduto.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"erro\": \"Nome do produto não informado.\"}");
                return;
            }

            PrecoMedioResponse response = dao.calcularPrecoMedio(usuarioId, nomeProduto);
            String json = gson.toJson(response);

            try (PrintWriter out = resp.getWriter()) {
                out.print(json);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"erro\": \"usuarioId inválido.\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}