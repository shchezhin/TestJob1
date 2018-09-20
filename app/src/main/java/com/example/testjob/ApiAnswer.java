package com.example.testjob;

import java.util.ArrayList;

public class ApiAnswer {
    private int status;
    private ArrayList<String> errors;

    public int getStatus() {
        return status;
    }

    public ArrayList<String> getErrors() {
        return errors;
    }
}
