package com.joel.omnicron.worker.util;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public final class TemplateRenderer {
    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();

    private TemplateRenderer() {
    }

    public static String render(String template, Map<String, Object> values) {
        Mustache renderer = MUSTACHE_FACTORY.compile(
                new StringReader(template),
                "job-template");

        StringWriter writer = new StringWriter();
        renderer.execute(writer, values);
        return writer.toString();
    }
}