package com.genuineflix.metal.api.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

import com.genuineflix.metal.api.data.collections.DataCompound;

public class IOHelper {

	public static DataCompound readCompressed(final InputStream stream) throws IOException {
		final DataInputStream compressedInput = new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
		DataCompound data;
		try {
			data = readStream(compressedInput, SizeLimit.NONE);
		}
		finally {
			compressedInput.close();
		}
		return data;
	}

	public static void writeCompressed(final DataCompound compound, final OutputStream stream) throws IOException {
		final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(stream)));
		try {
			writeToOutput(compound, output);
		}
		finally {
			output.close();
		}
	}

	public static DataCompound readCompressedBytes(final byte[] bs, final SizeLimit limit) throws IOException {
		final DataInputStream compressedInput = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bs))));
		DataCompound compound;
		try {
			compound = readStream(compressedInput, limit);
		}
		finally {
			compressedInput.close();
		}
		return compound;
	}

	public static byte[] compressToBytes(final DataCompound compound) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final DataOutputStream compressedOutput = new DataOutputStream(new GZIPOutputStream(baos));
		try {
			writeToOutput(compound, compressedOutput);
		}
		finally {
			compressedOutput.close();
		}
		return baos.toByteArray();
	}

	public static void saveToFileSafe(final DataCompound compound, final File file) throws IOException {
		final File tmp = new File(file.getAbsolutePath() + "_tmp");
		if (tmp.exists())
			tmp.delete();
		saveToFile(compound, tmp);
		if (file.exists())
			file.delete();
		if (file.exists())
			throw new IOException("Failed to delete " + file);
		else
			tmp.renameTo(file);
	}

	public static DataCompound readStream(final DataInputStream stream) throws IOException {
		return readStream(stream, SizeLimit.NONE);
	}

	public static DataCompound readStream(final DataInput input, final SizeLimit limit) throws IOException {
		final Data nbtbase = getData(input, 0, limit);
		if (nbtbase instanceof DataCompound)
			return (DataCompound) nbtbase;
		else
			throw new IOException("Root tag must be a named compound tag");
	}

	private static void writeToOutput(final Data data, final DataOutput output) throws IOException {
		output.writeByte(data.getTypeByte());
		if (data.getTypeByte() != 0) {
			output.writeUTF("");
			data.write(output);
		}
	}

	private static Data getData(final DataInput input, final int depth, final SizeLimit limit) throws IOException {
		final byte type = input.readByte();
		if (type == 0)
			return new Data.DataEnd();
		else {
			input.readUTF();
			final Data nbtbase = Data.create(type);
			try {
				nbtbase.read(input, depth, limit);
				return nbtbase;
			}
			catch (final IOException ioexception) {
				final CrashReport report = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
				final CrashReportCategory category = report.makeCategory("NBT Tag");
				category.addCrashSection("Tag name", Data.TYPES[type]);
				category.addCrashSection("Tag type", Byte.valueOf(type));
				throw new ReportedException(report);
			}
		}
	}

	public static void saveToFile(final DataCompound compound, final File file) throws IOException {
		final DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
		try {
			writeToOutput(compound, stream);
		}
		finally {
			stream.close();
		}
	}

	public static DataCompound open(final File file) throws IOException {
		return open(file, SizeLimit.NONE);
	}

	public static DataCompound open(final File file, final SizeLimit limit) throws IOException {
		if (!file.exists())
			return null;
		else {
			final DataInputStream input = new DataInputStream(new FileInputStream(file));
			DataCompound compound;
			try {
				compound = readStream(input, limit);
			}
			finally {
				input.close();
			}
			return compound;
		}
	}
}