package com.dispensa.controller;

import com.dispensa.model.OcrSugestao;
import com.dispensa.util.OcrDanfeParser;
import com.google.gson.Gson;

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

@WebServlet("/api/danfe/ocr")
@MultipartConfig
public class OcrDanfeServlet extends HttpServlet {

    private final Gson gson = new Gson();

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Part filePart = req.getPart("imagem"); // precisa bater com o name do input no HTML
            if (filePart == null) {
                throw new IllegalArgumentException("Nenhuma imagem enviada com o nome 'imagem'.");
            }

            byte[] imagemBytes;
            try (InputStream is = filePart.getInputStream()) {
                imagemBytes = is.readAllBytes();
            }

            OcrSugestao sugestao = OcrDanfeParser.processarImagem(imagemBytes, filePart.getSubmittedFileName());

            try (PrintWriter out = resp.getWriter()) {
                out.print(gson.toJson(sugestao));
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Erro desconhecido";
                out.print("{\"erro\": \"" + msg + "\"}");
            }
        }
    }
}