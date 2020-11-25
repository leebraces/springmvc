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
		// ��ʼ����Ҫ�����ļ��ļ�����
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding(encoding);
		if (!ServletFileUpload.isMultipartContent(request)) {
			return items;
		}
		try {
			// ʹ��ServletFileUpload�����������ϴ����ݣ�����������ص���һ��List<FileItem>����
			// ÿһ��FileItem��Ӧһ��Form����������
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem item : list) {
				// ���fileitem�з�װ������ͨ�����������
				if (item.isFormField()) {
					// String name = item.getFieldName();
					// //�����ͨ����������ݵ�������������
				} else {
					String fileName = item.getName();
					if (fileName == null || fileName.trim().equals("")) {
						continue;
					}
					// ע�⣺��ͬ��������ύ���ļ����ǲ�һ���ģ���Щ������ύ�������ļ����Ǵ���·���ģ�
					// �磺 c:\a\b\1.txt������Щֻ�ǵ������ļ������磺1.txt
					// �����ȡ�����ϴ��ļ����ļ�����·�����֣�ֻ�����ļ�������
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
		// ȡ��һ����ʵ��
		FileItem item = items.get(0);

		// �ϴ����ļ���
		String fileName = item.getName();
		fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);

		// ��������ȡ,�����������������ɽ��� , ���� �Ȳ���
		InputStream in = item.getInputStream();
		// ����һ���ļ������
		File file = new File("E:/file/" + UUID.randomUUID().toString()+fileName);
		OutputStream fos = new FileOutputStream(file);

		// ����һ��������

		byte buffer[] = new byte[1024];

		// �ж��������е������Ƿ��Ѿ�����ı�ʶ

		int length = 0;

		// ѭ�������������뵽���������У�(len=in.read(buffer))>0�ͱ�ʾin���滹������

		while ((length = in.read(buffer)) > 0) {

			// ʹ��FileOutputStream�������������������д�뵽ָ����Ŀ¼(savePath + "\\" + filename)����

			fos.write(buffer, 0, length);

		}
		// �ر���
		in.close();

		// ���һ��Ҫɾ��,item����ʱ�ļ�
		item.delete();
	}
}
