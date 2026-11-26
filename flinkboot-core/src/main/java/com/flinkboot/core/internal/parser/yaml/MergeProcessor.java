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
        var rootNode = CaseInsensitiveObjectNode.from(task.root());
        for (var entry : task.target().properties()) {
            var key = entry.getKey();
            var newValue = entry.getValue();
            var existingOpt = rootNode.find(key);
            if (existingOpt.isPresent()) {
                var existing = existingOpt.get();
                if (existing.value().isObject() && newValue.isObject()) {
                    tasks.addLast(new MergeTask(task.pathOf(existing.key()), (ObjectNode) existing.value(), (ObjectNode) newValue));
                    continue;
                }
                if (existing.value().isArray() && newValue.isArray() && features.listMerging()) {
                    var existingArray = (ArrayNode) existing.value();
                    for (var elem : newValue) {
                        existingArray.add(placeholderResolver.resolve(elem));
                    }
                    continue;
                }
                if (!features.permitOverride()) {
                    throw new YamlParsingException("Overriding an existing value is forbidden: " + task.pathOf(existing.key()));
                }
                key = existing.key();
            }
            if (newValue.isObject()) {
                var childNode = task.root().objectNode();
                rootNode.put(key, childNode);
                tasks.addLast(new MergeTask(task.pathOf(key), childNode, (ObjectNode) newValue));
            } else {
                rootNode.put(key, placeholderResolver.resolve(newValue));
            }
        }
    }

}
