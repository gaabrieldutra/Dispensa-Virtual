package com.dispensa.model;

import java.util.List;

public class OcrSpaceResponse {
    public List<ParsedResult> ParsedResults;
    public boolean IsErroredOnProcessing;
    public String ErrorMessage;

    public static class ParsedResult {
        public String ParsedText;
    }
}
