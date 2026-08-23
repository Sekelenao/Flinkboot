package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;

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

    private void processTask(MergeTask task){
        for (var entry: task.target().properties()){
            var key = entry.getKey();
            var newValue = entry.getValue();
            var existingValue = task.root().get(key);
            if (existingValue == null) {
                task.root().set(key, placeholderResolver.resolve(newValue));
            } else if (existingValue.isObject() && newValue.isObject()) {
                tasks.push(new MergeTask(task.pathOf(key), (ObjectNode) existingValue, (ObjectNode) newValue));
            } else if (existingValue.isArray() && newValue.isArray() && features.listMerging()) {
                var existingArray = (ArrayNode) existingValue;
                var newArray = (ArrayNode) newValue;
                for (var elem : newArray) {
                    existingArray.add(placeholderResolver.resolve(elem));
                }
            } else if (features.permitOverride()) {
                task.root().set(key, placeholderResolver.resolve(newValue));
            } else {
                throw new YamlParsingException("Overriding an existing value is forbidden: " + task.pathOf(key));
            }
        }
    }

}
