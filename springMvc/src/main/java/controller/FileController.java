package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import annotation.MyController;
import annotation.MyRequestMapping;
import utils.FileHandlle;

@MyController
public class FileController {

	@MyRequestMapping("upload")
	public String forward(HttpServletRequest req, HttpServletResponse resp) {
		System.out.println("upload...");

		// HttpServletRequest request = WebContext.requestHodler.get();

		// 执行完forward1方法之后返回的视图
		return "fileUpload";

	}

	@MyRequestMapping("fileUpload")
	public String testUpload(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		System.out.println("fileupload...");

		resp.setContentType("text/html;charset=utf-8");
		resp.getWriter().write("上传成功！");
		FileHandlle.Do(req, "utf-8");
		return null;
	}

}
