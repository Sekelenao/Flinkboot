package io.github.sekelenao.flinkboot.core.internal.parser;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;

import java.util.ArrayDeque;
import java.util.Objects;

public final class FusionProcessor {

    private final ArrayDeque<FusionTask> tasks = new ArrayDeque<>();

    private final ObjectNode root;

    private final FusionFeatures features;

    public FusionProcessor(ObjectNode root, FusionFeatures features){
        this.root = Objects.requireNonNull(root);
        this.features = Objects.requireNonNull(features);
    }

    public void apply(ObjectNode target){
        Objects.requireNonNull(target);
        tasks.add(new FusionTask("", root, target));
        while (!tasks.isEmpty()){
            processTask(tasks.pop());
        }
    }

    private void processTask(FusionTask task){
        for (var entry: task.target().properties()){
            var key = entry.getKey();
            var newValue = entry.getValue();
            var existingValue = task.root().get(key);
            if (existingValue == null) {
                task.root().set(key, newValue);
            } else if (existingValue.isObject() && newValue.isObject()) {
                tasks.push(new FusionTask(task.pathOf(key), (ObjectNode) existingValue, (ObjectNode) newValue));
            } else if (existingValue.isArray() && newValue.isArray() && features.listFusion()) {
                var existingArray = (ArrayNode) existingValue;
                var newArray = (ArrayNode) newValue;
                existingArray.addAll(newArray);
            } else if (features.permitOverride()) {
                task.root().set(key, newValue);
            } else {
                throw new YamlParsingException("Overriding an existing value is forbidden: " + task.pathOf(key));
            }
        }
    }

}
