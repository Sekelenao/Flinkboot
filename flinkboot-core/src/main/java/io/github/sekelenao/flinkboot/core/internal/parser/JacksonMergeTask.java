package io.github.sekelenao.flinkboot.core.internal.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Objects;

public final class JacksonMergeTask {

    private final String path;

    private final ObjectNode root;

    private final ObjectNode target;

    public JacksonMergeTask(String path, ObjectNode root, ObjectNode target) {
        this.path = Objects.requireNonNull(path);
        this.root = Objects.requireNonNull(root);
        this.target = Objects.requireNonNull(target);
    }

    public ObjectNode root() {
        return root;
    }

    public ObjectNode target() {
        return target;
    }

    public String pathOf(String key) {
        return path.isEmpty() ? key : path + "." + key;
    }

}
