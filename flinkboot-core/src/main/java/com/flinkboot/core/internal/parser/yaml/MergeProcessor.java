package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flinkboot.core.api.exception.parsing.YamlParsingException;

import java.util.ArrayDeque;
import java.util.Objects;

public final class MergeProcessor {

    private final ArrayDeque<MergeTask> tasks = new ArrayDeque<>();

    private final ObjectNode root;

    private final ParserFeatures features;

    private final PlaceholderResolver placeholderResolver;

    public MergeProcessor(ObjectNode root, ParserFeatures features, PlaceholderResolver placeholderResolver){
        this.root = Objects.requireNonNull(root);
        this.features = Objects.requireNonNull(features);
        this.placeholderResolver = Objects.requireNonNull(placeholderResolver);
    }

    public void apply(ObjectNode target){
        Objects.requireNonNull(target);
        tasks.add(new MergeTask("", root, target));
        while (!tasks.isEmpty()){
            processTask(tasks.pop());
        }
    }

    private void processTask(MergeTask task) {
        var rootNode = new CaseInsensitiveObjectNode(task.root());
        for (var entry : task.target().properties()) {
            var key = entry.getKey();
            var newValue = entry.getValue();
            var existingOpt = rootNode.find(key);
            if (existingOpt.isEmpty()) {
                rootNode.put(key, placeholderResolver.resolve(newValue));
                continue;
            }
            var existing = existingOpt.get();
            if (existing.value().isObject() && newValue.isObject()) {
                tasks.push(new MergeTask(task.pathOf(existing.key()), (ObjectNode) existing.value(), (ObjectNode) newValue));
            } else if (existing.value().isArray() && newValue.isArray() && features.listMerging()) {
                var existingArray = (ArrayNode) existing.value();
                var newArray = (ArrayNode) newValue;
                for (var elem : newArray) {
                    existingArray.add(placeholderResolver.resolve(elem));
                }
            } else if (features.permitOverride()) {
                rootNode.put(existing.key(), placeholderResolver.resolve(newValue));
            } else {
                throw new YamlParsingException("Overriding an existing value is forbidden: " + task.pathOf(existing.key()));
            }
        }
    }

}
