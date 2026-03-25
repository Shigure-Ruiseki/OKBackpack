package ruiseki.okbackpack.common.helpers;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonElement;

import ruiseki.okcore.json.AbstractJsonReader;

/**
 * Reader for a single Backpack template JSON file.
 */
public class BackpackJsonReader extends AbstractJsonReader<BackpackMaterial> {

    public BackpackJsonReader(File path) {
        super(path);
    }

    @Override
    public BackpackMaterial read() throws IOException {
        if (!path.exists()) {
            return null;
        }
        return readFile(path);
    }

    @Override
    protected BackpackMaterial readFile(JsonElement root, File file) {
        if (root.isJsonObject()) {
            BackpackMaterial material = new BackpackMaterial();
            material.setSourceFile(file);
            material.read(root.getAsJsonObject());
            return material;
        }
        return null;
    }
}
