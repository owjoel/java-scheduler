package com.joel.omnicron.worker.controller;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class JobTemplateRenderer {
    private final MustacheFactory mustacheFactory;

    public JobTemplateRenderer(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
    }

    public String render(String template, Map<String, Object> optionValues) {
        Mustache renderer = mustacheFactory.compile(
            new StringReader(template),
            "job-template");
        StringWriter writer = new StringWriter();
        renderer.execute(writer, optionValues);
        return writer.toString();
    }
}
