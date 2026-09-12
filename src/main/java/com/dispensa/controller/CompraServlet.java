package com.dispensa.controller;

import com.dispensa.dao.CompraDAO;
import com.dispensa.model.CompraRequest;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.stream.Collectors;

@WebServlet("/api/compras/registrar")
public class CompraServlet extends HttpServlet {

    private final CompraDAO dao = new CompraDAO();
    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Lê o corpo (JSON) enviado pelo front
        String corpoJson;
        try (BufferedReader reader = req.getReader()) {
            corpoJson = reader.lines().collect(Collectors.joining());
        }

        CompraRequest compra = gson.fromJson(corpoJson, CompraRequest.class);

        try (PrintWriter out = resp.getWriter()) {
            int notaId = dao.registrarCompraCompleta(compra);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"sucesso\": true, \"notaId\": " + notaId + "}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"sucesso\": false, \"erro\": \"" + e.getMessage() + "\"}");
            }
        }
    }
}