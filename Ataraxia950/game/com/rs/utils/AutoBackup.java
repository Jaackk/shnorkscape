package com.rs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AutoBackup {

	public static String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("MM dd yyyy");
		Date date = new Date();
		String currentDate = dateFormat.format(date);
		date = null;
		dateFormat = null;
		return currentDate;
	}

	private static final void zip(File directory, File base, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[20000];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory())
				zip(files[i], base, zos);
			else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	public static final void zipDirectory(File f, File zf) throws IOException {
		ZipOutputStream z = new ZipOutputStream(new FileOutputStream(zf));
		zip(f, f, z);
		z.close();
	}

	public static void init() {
		File f1 = new File("data/playersaves/characters/");
		File f2 = new File("data/playersaves/charactersfullBackup/mainsave " + getDate() + ".zip");
		if (!f2.exists()) {
			try {
				if (f1.list().length == 0) {
					Logger.getGlobal().info("[Auto-Backup] The characters folder is empty.");
					return;
				}
				Logger.getGlobal().info(Utils.getFormattedNumber(f1.list().length) + " "
						+ (f1.list().length > 1 ? "accounts" : "account") + " " + (f1.list().length > 1 ? "are" : "is")
						+ " being added to the backup. " + f2.getName() + ".");
				zipDirectory(f1, f2);
			} catch (Exception e) {
				Logger.getGlobal().catching(e);
			}
		}
	}
}