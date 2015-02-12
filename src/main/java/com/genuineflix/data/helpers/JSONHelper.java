package com.genuineflix.data.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import com.genuineflix.data.AbstractData;
import com.genuineflix.data.IData;
import com.genuineflix.data.collections.DataCompound;
import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONHelper {

	private static final Gson GSON;
	private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
	private static final Type TYPE = new TypeToken<Map<String, IData>>() {}.getType();

	public static void saveDataToJSON(final DataCompound compound, final File file) {
		try {
			if (!file.exists())
				file.createNewFile();
			final Writer wr = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
			GSON.toJson(compound, DataCompound.class, wr);
			wr.close();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static DataCompound loadFromJSON(final File file) {
		DataCompound out = null;
		if (!file.exists())
			return out;
		try {
			final FileReader fr = new FileReader(file);
			out = GSON.fromJson(fr, DataCompound.class);
			fr.close();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return out;
	}

	static {
		GSON_BUILDER.setPrettyPrinting();
		GSON_BUILDER.enableComplexMapKeySerialization();
		for (final AbstractData data : AbstractData.TYPES)
			GSON_BUILDER.registerTypeAdapter(data.getClass(), data);
		GSON = GSON_BUILDER.create();
	}
}
