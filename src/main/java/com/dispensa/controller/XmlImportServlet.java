package com.dispensa.controller;

import com.dispensa.dao.CompraDAO;
import com.dispensa.model.CompraRequest;
import com.dispensa.util.NfeXmlParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@WebServlet("/api/danfe/importar-xml")
@MultipartConfig
public class XmlImportServlet extends HttpServlet {

    private final CompraDAO compraDao = new CompraDAO();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Part filePart = req.getPart("arquivoXml"); // precisa bater com o "name" do input no HTML
            if (filePart == null) {
                throw new IllegalArgumentException("Nenhum arquivo enviado com o nome 'arquivoXml'.");
            }

            try (InputStream xmlInput = filePart.getInputStream()) {
                CompraRequest compra = NfeXmlParser.parse(xmlInput);
                compra.setUsuarioId(1); // TODO: trocar pelo usuário logado de verdade no futuro

                int notaId = compraDao.registrarCompraCompleta(compra);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                try (PrintWriter out = resp.getWriter()) {
                    out.print("{\"sucesso\": true, \"notaId\": " + notaId + "}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro desconhecido";
                out.print("{\"sucesso\": false, \"erro\": \"" + msg + "\"}");
            }
        }
    }
}
