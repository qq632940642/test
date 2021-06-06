package zx.androidUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;
import android.util.Xml;

public class FileUtils {
	public final static boolean isExists(String path) {
		if (path == null)
			return false;
		File file = new File(path);
		if (file.exists())
			return true;
		return false;
	}

	public final static int getConnectTimeout() {
		return 5000;
	}

	public final static int getReadTimeout() {
		return 3000;
	}

	public final static long getFileChecksum(String fileName) {
		if (TextUtils.isEmpty(fileName))
			return 0;
		File file = new File(fileName);
		return getFileChecksum(file);
	}

	public final static long getFileChecksum(File file){
		if(file == null){
			return 0;
		}
		if (!file.exists() || !file.isFile()){
			file = null;
			return 0;
		}
		long Checksum = 0xffffffff;
		byte[] buf = new byte[2048];
		CheckedInputStream cis = null;
		try {
			cis = new CheckedInputStream(new FileInputStream(file), new CRC32());
			while (cis.read(buf) >= 0) {
			}
			Checksum = cis.getChecksum().getValue();
			cis = null;
			file = null;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return Checksum;
	}
	public final static InputStream openStream(final URL url)
			throws IOException {
		final HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setConnectTimeout(getConnectTimeout());
		http.setReadTimeout(getReadTimeout());
		return http.getInputStream();
	}

	public final static XmlPullParser openStream(final InputStream in,
			String inputEncoding) throws XmlPullParserException {
		final XmlPullParser parser = Xml.newPullParser();
		parser.setInput(in, inputEncoding);
		return parser;
	}
}
