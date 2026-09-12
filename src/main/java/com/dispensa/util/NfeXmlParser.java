package com.dispensa.util;

import com.dispensa.model.CompraRequest;
import com.dispensa.model.ItemCompra;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NfeXmlParser {

    public static CompraRequest parse(InputStream xmlInput) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // NF-e usa namespace; isso deixa a leitura funcionar ignorando o prefixo
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlInput);
        doc.getDocumentElement().normalize();

        CompraRequest compra = new CompraRequest();
        compra.setOrigem("XML");

        compra.setNumeroNota(getTagValue(doc, "nNF"));
        compra.setSerie(getTagValue(doc, "serie"));
        compra.setFornecedor(getTagValue(doc, "xNome"));

        String dhEmi = getTagValue(doc, "dhEmi");
        compra.setDataEmissao(dhEmi != null && dhEmi.length() >= 10 ? dhEmi.substring(0, 10) : null);

        String vNF = getTagValue(doc, "vNF");
        compra.setValorTotal(vNF != null ? Double.parseDouble(vNF) : 0);

        // Chave de acesso vem do atributo Id da tag infNFe, ex: "NFe3519..." (44 dígitos depois do prefixo)
        NodeList infNFeList = doc.getElementsByTagNameNS("*", "infNFe");
        if (infNFeList.getLength() > 0) {
            String idAttr = ((Element) infNFeList.item(0)).getAttribute("Id");
            compra.setChaveAcesso(idAttr != null && idAttr.startsWith("NFe") ? idAttr.substring(3) : idAttr);
        }

        List<ItemCompra> itens = new ArrayList<>();
        NodeList detNodes = doc.getElementsByTagNameNS("*", "det");

        for (int i = 0; i < detNodes.getLength(); i++) {
            Element det = (Element) detNodes.item(i);
            NodeList prodList = det.getElementsByTagNameNS("*", "prod");
            if (prodList.getLength() == 0) continue;
            Element prod = (Element) prodList.item(0);

            ItemCompra item = new ItemCompra();
            item.setProdutoNome(getChildText(prod, "xProd"));

            String qtd = getChildText(prod, "qCom");
            item.setQuantidade(qtd != null ? Double.parseDouble(qtd) : 1);

            String precoUn = getChildText(prod, "vUnCom");
            item.setPrecoUnitario(precoUn != null ? Double.parseDouble(precoUn) : 0);

            item.setDataCompra(compra.getDataEmissao());
            itens.add(item);
        }

        compra.setItens(itens);
        return compra;
    }

    private static String getTagValue(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    private static String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }
}