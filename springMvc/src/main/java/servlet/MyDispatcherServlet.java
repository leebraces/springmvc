package servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import annotation.MyController;
import annotation.MyRequestMapping;

public class MyDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Properties properties = new Properties(); //spring�����ļ���Ķ���

	private List<String> classNames = new ArrayList<String>();   //ɨ��·���µ������б�

	private Map<String, Object> ioc = new HashMap<String, Object>();  //IOC��������,����ע���ʵ������

	private Map<String, Method> handlerMapping = new HashMap<String, Method>(); // handlerMapping���󣬴洢url�ͷ�����ӳ���ϵ

	private Map<String, Object> controllerMap = new HashMap<String, Object>(); //����url��ע���bean

	@Override
	public void init(ServletConfig config) throws ServletException {

		// 1.���������ļ�
		doLoadConfig(config.getInitParameter("contextConfigLocation"));

		// 2.��ʼ���������������,ɨ���û��趨�İ��������е���
		doScanner(properties.getProperty("scanPackage"));

		// 3.�õ�ɨ�赽����,ͨ���������,ʵ����,���ҷŵ�ioc������
		doInstance();

		// 4.��ʼ��HandlerMapping(��url��method��Ӧ��)
		initHandlerMapping();

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			// ��������
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter().write("500!! Server Exception");
		}

	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		if (handlerMapping.isEmpty()) {
			return;
		}

		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		//ȥ��URL�ж����"/"
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		 //������ʵ�URL�����ڣ���ֱ�ӷ���404����
		if (!this.handlerMapping.containsKey(url)) {
			resp.getWriter().write("404 NOT FOUND!");
			return;
		}

		Method method = this.handlerMapping.get(url);

		// ��ȡ�����Ĳ����б�
		Class<?>[] parameterTypes = method.getParameterTypes();
		System.out.println("��ȡ�Ĳ����б�Ϊ��"+parameterTypes);
		// ��ȡ����Ĳ���
		Map<String, String[]> parameterMap = req.getParameterMap();

		// �������ֵ
		Object[] paramValues = new Object[parameterTypes.length];
		// �����Ĳ����б�
		for (int i = 0; i < parameterTypes.length; i++) {
			// ���ݲ������ƣ���ĳЩ����
			String requestParam = parameterTypes[i].getSimpleName();
			System.out.println("��servlet�л�ȡ�Ĳ�������Ϊ��"+requestParam);
			if (requestParam.equals("HttpServletRequest")) {
				// ������������ȷ��ǿת����
				paramValues[i] = req;
				continue;
			}
			if (requestParam.equals("HttpServletResponse")) {
				paramValues[i] = resp;
				continue;
			}
			//������Ĳ���
			if (requestParam.equals("String")) {
				for (Entry<String, String[]> param : parameterMap.entrySet()) {
					 String value =Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
					paramValues[i] = value;
				}
			}
		}
		// ���÷������������
		try {
			Object object=method.invoke(this.controllerMap.get(url), paramValues);// ��һ��������method����Ӧ��ʵ�� ��ioc������
			System.out.println("�õ���objectΪ��"+object);
			//������ֵ����ͼ
			if(object!=null) {
				System.out.println("��ַΪ��/WEB-INF/"+object.toString()+".jsp");
				//resp.sendRedirect(object.toString()+".jsp");
				req.getRequestDispatcher(object.toString()+".jsp").forward(req, resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doLoadConfig(String location) {
		// ��web.xml�е�contextConfigLocation��Ӧvalueֵ���ļ����ص�������
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			// ��Properties�ļ������ļ��������
			properties.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// ����
			if (null != resourceAsStream) {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void doScanner(String packageName) {
		// �����е�.�滻��/
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				// �ݹ��ȡ��
				doScanner(packageName + "." + file.getName());
			} else {
				String className = packageName + "." + file.getName().replace(".class", "");
				classNames.add(className);
			}
		}
	}

	private void doInstance() {
		if (classNames.isEmpty()) {
			return;
		}
		for (String className : classNames) {
			try {
				// ��������,������ʵ����(ֻ�м�@MyController��Ҫʵ����)
				Class<?> clazz = Class.forName(className);
				if (clazz.isAnnotationPresent(MyController.class)) {
					ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
				} else {
					continue;
				}

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private void initHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}
		try {
			for (Entry<String, Object> entry : ioc.entrySet()) {
				Class<? extends Object> clazz = entry.getValue().getClass();
				if (!clazz.isAnnotationPresent(MyController.class)) {
					continue;
				}

				// ƴurlʱ,��controllerͷ��urlƴ�Ϸ����ϵ�url
				String baseUrl = "";
				if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
					MyRequestMapping annotation = clazz.getAnnotation(MyRequestMapping.class);
					baseUrl = annotation.value();
				}
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					if (!method.isAnnotationPresent(MyRequestMapping.class)) {
						continue;
					}
					MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
					String url = annotation.value();

					url = (baseUrl + "/" + url).replaceAll("/+", "/");
					handlerMapping.put(url, method);
					controllerMap.put(url, clazz.newInstance());
					System.out.println(url + "," + method);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ���ַ���������ĸСд
	 * 
	 * @param name
	 * @return
	 */
	private String toLowerFirstWord(String name) {
		char[] charArray = name.toCharArray();
		charArray[0] += 32;
		return String.valueOf(charArray);
	}

}