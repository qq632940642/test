package zx.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


/**
 * IO的工具类
 * @author zx
 * */
public class IOUtils {
	/** 文件的复制功能, 支持文件到文件的复制 */
	public static void cp(String src, String dst) {
		cp(new File(src), new File(dst));
	}

	public static void cp(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
			byte[] buf = new byte[1024 * 512];// 1K
			int n;
			while ((n = in.read(buf)) != -1) {
				System.out.println(n);
				out.write(buf, 0, n);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void cp1(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/** 将文件内容按照16进制输出到控制台,每16个byte为一行 */
	public static void print(String file) throws IOException {
		InputStream in = new FileInputStream(file);
		int b;
		int i = 1;
		while ((b = in.read()) != -1) {// 如果读取的不是文件结束继续读
			if (b <= 0xf) {// 如果是一位16进制数 补充一个"0"
				System.out.write("0".getBytes("GBK"));
			}
			System.out.print(Integer.toHexString(b) + " ");
			if (i++ % 16 == 0) {// 每16个输出一个折行
				System.out.println();
			}
		}
		System.out.println();
		in.close();
	}

	/**
	 * 将文件切分为 指定大小的系列文件 如: IOUtiles.split("test.zip", 1024) 将文件 test.zip 切分为
	 * 1024K(1M) 大小的 系列文件 : test.zip.0, test.zip.1,...
	 * 
	 * @param filename
	 *            源文件名
	 * @param size
	 *            文件大小, 以k为单位
	 */
	public static void split(String file, int size) throws IOException {
		if (size <= 0) {
			throw new IllegalArgumentException("搞啥呀!");
		}
		int idx = 0;// 文件的序号
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file
				+ "." + idx++));
		int b;
		int count = 0;
		while ((b = in.read()) != -1) {
			out.write(b);
			count++;
			if (count % (size * 1024) == 0) {
				out.close();
				out = new BufferedOutputStream(new FileOutputStream(file + "."
						+ idx++));
			}
		}
		in.close();
		out.close();
	}

	/**
	 * 连接上面方法的系列文件为一个文件 如: IOUtiles.join("test.zip.0"); 在硬盘上生成一个没有序号的文件:
	 * test.zip
	 * 
	 * @param basename
	 *            第一个文件, 如test.zip.0
	 */
	public static void join(String file) throws IOException {
		String filename = file.substring(0, file.lastIndexOf("."));
		String num = file.substring(file.lastIndexOf(".") + 1);
		int idx = Integer.parseInt(num);
		OutputStream out = new FileOutputStream(filename);
		File f = new File(filename + "." + idx++);
		while (f.exists()) {
			InputStream in = new FileInputStream(f);
			cp(in, out);
			in.close();
			f = new File(filename + "." + idx++);
		}
		out.close();
	}

	/** 复制文件, 复制in到out, 不关闭流 */
	public static void cp(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024 * 512];// 512K缓冲
		int count;
		while ((count = in.read(buf)) != -1) {// 读取到buf
			// System.out.println(count);//
			out.write(buf, 0, count); // 写入到输出流
		}
		out.flush(); // 刷出缓冲到目标流
	}

	/** 对象的深层复制,对象需要实现序列化接口 */
	public static Object deepCopy(Object obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(obj);
			oos.close();
			byte[] buf = out.toByteArray();
			ByteArrayInputStream in = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(in);
			Object o = ois.readObject();
			in.close();
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
}
