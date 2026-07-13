package io.github.sekelenao.flinkboot.core.internal.parser;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;

import java.util.ArrayDeque;
import java.util.Objects;

public final class JacksonFusionProcessor {

    private final ArrayDeque<JacksonMergeTask> tasks = new ArrayDeque<>();

    private final ObjectNode root;

    public JacksonFusionProcessor(ObjectNode root){
        this.root = Objects.requireNonNull(root);
    }

    public void apply(ObjectNode target){
        Objects.requireNonNull(target);
        tasks.add(new JacksonMergeTask("", root, target));
        while (!tasks.isEmpty()){
            processTask(tasks.pop());
        }
    }

    private void processTask(JacksonMergeTask task){
        for (var entry: task.target().properties()){
            var key = entry.getKey();
            var newValue = entry.getValue();
            var existingValue = task.root().get(key);
            if (existingValue == null) {
                task.root().set(key, newValue);
            } else if (existingValue.isObject() && newValue.isObject()) {
                tasks.push(new JacksonMergeTask(task.pathOf(key), (ObjectNode) existingValue, (ObjectNode) newValue));
            } else {
                throw new YamlParsingException("Overriding an existing value is forbidden: " + task.pathOf(key));
            }
        }
    }

}
