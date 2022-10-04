package com.example.sample.api.form;

import lombok.Data;

@Data
public class EmailForm {
    private String from;
    private String to;
    private String subject;
    private String body;
    private String file;

}
