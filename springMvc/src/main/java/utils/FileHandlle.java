package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileHandlle {
	public static List<FileItem> getUploadInputSteam(HttpServletRequest request, String encoding) {
		if (encoding == null || "".equals(encoding)) {
			encoding = "UTF-8";
		}
		List<FileItem> items = new ArrayList<FileItem>();
		// 初始化需要解析文件的几个类
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding(encoding);
		if (!ServletFileUpload.isMultipartContent(request)) {
			return items;
		}
		try {
			// 使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合
			// 每一个FileItem对应一个Form表单的输入项
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				// 如果fileitem中封装的是普通输入项的数据
				if (item.isFormField()) {
					// String name = item.getFieldName();
					// //解决普通输入项的数据的中文乱码问题
				} else {
					String fileName = item.getName();
					if (fileName == null || fileName.trim().equals("")) {
						continue;
					}
					// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，
					// 如： c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
					// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					// fileName = fileName.substring(fileName.lastIndexOf("\\")+1);
					items.add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return items;
		}
		return items;
	}

	public static void Do(HttpServletRequest request, String encoding) throws IOException {
		List<FileItem> items = getUploadInputSteam(request, "utf-8");
		// 取第一个做实验
		FileItem item = items.get(0);

		// 上传的文件名
		String fileName = item.getName();
		fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);

		// 数据流获取,获得数据流你可以自由解析 , 保存 等操作
		InputStream in = item.getInputStream();
		// 创建一个文件输出流
		File file = new File("E:/file/" + UUID.randomUUID().toString()+fileName);
		OutputStream fos = new FileOutputStream(file);

		// 创建一个缓冲区

		byte buffer[] = new byte[1024];

		// 判断输入流中的数据是否已经读完的标识

		int length = 0;

		// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据

		while ((length = in.read(buffer)) > 0) {

			// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\" + filename)当中

			fos.write(buffer, 0, length);

		}
		// 关闭流
		in.close();

		// 最后一定要删除,item的临时文件
		item.delete();
	}
}
