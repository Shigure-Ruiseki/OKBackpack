package ruiseki.okbackpack.common.helpers;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonObject;

import ruiseki.okcore.json.AbstractJsonWriter;

/**
 * Writer for a single Backpack template JSON file.
 */
public class BackpackJsonWriter extends AbstractJsonWriter<BackpackMaterial> {

    public BackpackJsonWriter(File path) {
        super(path);
    }

    @Override
    protected void writeSingle(BackpackMaterial material) throws IOException {
        File targetFile = path.isDirectory() ? new File(path, "default.json") : path;
        JsonObject json = new JsonObject();
        material.write(json);
        writeObjectToFile(targetFile, json);
    }
}
