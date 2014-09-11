package rrdbrowser;


import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.*;
import com.google.common.io.*;

public class RrdServlet extends HttpServlet {
	
  static String q(String text) {
		return "\"" + text + "\"";
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
    response.setDateHeader("Expires", 0); // prevents caching at proxy server
		
		try {
			
		List<String> args = Lists.newArrayList();
		args.add("rrdtool");
		args.add("graph");
		args.add("-");
		args.add("-D");
		args.add("-E");
		
//		String lower = request.getParameter("l");
//		if (lower != null)
//			cmdline += "-l " + lower + " ";
//		String upper = request.getParameter("u");
//		if (upper != null)
//			cmdline += "-u " + upper + " ";

		String width = request.getParameter("w");
		if (width != null) {
			args.add("-w"); args.add(width);
		}
		String height = request.getParameter("h");
		if (height != null) {
			args.add("-h"); args.add(height);
		}
		String start = request.getParameter("s");
		if (start != null) {
			args.add("-s"); args.add(start);
		}
		String start_end = request.getParameter("e");
		if (start_end != null) {
			args.add("-e"); args.add(start_end);
		}
		
      // hosts
      List<String> logicalHosts = Lists.newArrayList();
      if (request.getParameterValues("host") == null)
        logicalHosts.add("*");
      else
        logicalHosts.addAll(ImmutableList.copyOf(request.getParameterValues("host")));
		
      // plugin
      final String plugin = Objects.firstNonNull(request.getParameter("p"), "load");
		
      // plugin instances
      List<String> plugin_instances = Lists.newArrayList();
      if (request.getParameterValues("pi") == null)
        plugin_instances.add("*");
      else
        plugin_instances.addAll(ImmutableList.copyOf(request.getParameterValues("pi")));
		
		// type
		// get
      final String type = Objects.firstNonNull(request.getParameter("t"), plugin);
//      final String type = Objects.firstNonNull(request.getParameter("t"), RrdTools.getAllTypes(plugin, plugin_instances).get(0));
//		String type = request.getParameter("t");
//		if (!has(type))
////		  type = plugin;
//      type = ;
//		
      // type instances
      List<String> type_instances = Lists.newArrayList();
      if (request.getParameterValues("ti") == null)
        type_instances.add("*");
      else
        type_instances.addAll(ImmutableList.copyOf(request.getParameterValues("ti")));
		
		// dataSources
		List<String> params = getDataSourceMeta(type);
		if (params.size() == 0)
			params.add("value");
		
		String title = renderIdentifiers(logicalHosts, plugin, plugin_instances, type, type_instances, params);
		args.add("-t");
		args.add(q(title));
		args.add("-v");
		args.add(q("" + ImmutableList.of(params)));
		
		args.add("-c");
		args.add("BACK#000");
		args.add("-c");
		args.add("CANVAS#000");
		args.add("-c");
		args.add("SHADEA#000");
		args.add("-c");
		args.add("SHADEB#000");
		args.add("-c");
		args.add("GRID#000");
		args.add("-c");
		args.add("MGRID#555");
		args.add("-c");
		args.add("FONT#fff");
		args.add("-c");
		args.add("AXIS#eee");
		args.add("-c");
		args.add("FRAME#eee");
		args.add("-c");
		args.add("ARROW#eee");
		
		args.add("TEXTALIGN:center");

		int probeNum = -1;
		int cdef_value_index = -1;
		
		int deriveOrGauge = "GAUGE".equals(getDataSourceMetaType(type)) ? 1 : 0; // 0=derive, 1=gauge
		
		// hosts
		for (String logicalHost : logicalHosts) {
			List<String> physicalHosts = "*".equals(logicalHost)?RrdTools.getAllHosts():ImmutableList.of(logicalHost);
			// plugin
			// plugin instances
			for (String logicalPluginInstance : plugin_instances) {
			  List<String> physicalPluginInstances = "*".equals(logicalPluginInstance)?RrdTools.getAllPluginInstances(physicalHosts, plugin):ImmutableList.of(logicalPluginInstance);
				// type
				// type instances
				for (String logicalTypeInstance : type_instances) {
				  List<String> physicalTypeInstances = "*".equals(logicalTypeInstance)?RrdTools.getAllTypeInstances(physicalHosts, plugin, physicalPluginInstances, type):ImmutableList.of(logicalTypeInstance);
					// dataSources
					for (String param : params) {
						++cdef_value_index;
						List<String> cdef_value = Lists.newArrayList();
						if (deriveOrGauge==0)
							cdef_value.add("0");
						for (String physicalHost : physicalHosts) {
							for (String plugin_instance : physicalPluginInstances) {
								for (String type_instance : physicalTypeInstances) {
									String rrd_file = RrdTools.getRrdRoot();
									rrd_file += "/" + physicalHost;
									rrd_file += "/" + plugin;
									if (plugin_instance.length() > 0)
										rrd_file += "-" + plugin_instance;
									rrd_file += "/" + type;
									if (type_instance.length() > 0)
										rrd_file += "-" + type_instance;
									rrd_file += ".rrd";
									if (new File(rrd_file).exists()) {
										++probeNum;
										args.add("DEF:" + param + probeNum + "=" + rrd_file + ":" + param + ":" + "AVERAGE");
										cdef_value.add(param+probeNum);
										// if counter then add/sum
										if (deriveOrGauge==0)
											cdef_value.add("ADDNAN");
									}
								}
							}
						}
						// if gauge then avg
						if (deriveOrGauge==1)
							cdef_value.addAll(ImmutableList.of(""+cdef_value.size(), "AVG"));
						args.add("CDEF:cdef_value"+cdef_value_index+"="+Joiner.on(",").join(cdef_value));
						String identifier = renderIdentifier(logicalHost, plugin, logicalPluginInstance, type, logicalTypeInstance, param);
						args.add("LINE2:cdef_value" + cdef_value_index + blackTheme.get(cdef_value_index % blackTheme.size()) + "a:" + identifier);
						
//						args.add("VDEF:cdef_value"+cdef_value_index+"max=cdef_value"+cdef_value_index+",MAXIMUM");
//						args.add("LINE1:cdef_value" + cdef_value_index +"max"+ colors.get(cdef_value_index % colors.size()) + "a:" + identifier);
						
						String format = "%.0lf%s";
						args.add("GPRINT:cdef_value" + cdef_value_index + ":AVERAGE:\"" + format + "\"");
					}
				}		
			}
		}
		
//		System.out.println(args);
		
//		Exec.asByteSource(args).read()
		
		ProcessBuilder builder = new ProcessBuilder(args);
		final Process p = builder.start();
		try {
			int rc = 0;
			// int rc = p.waitFor();
			if (rc == 0) {
				response.setContentType("image/png");
				int len = -1;
				byte[] b = new byte[1024];
				InputStream is = p.getInputStream();
				try {
					while ((len = is.read(b)) > 0) {
						response.getOutputStream().write(b, 0, len);
					}
				} finally {
					is.close();
				}
			} else {
				response.getWriter().println(args);
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				try {
					String str;
					while ((str = in.readLine()) != null)
						response.getWriter().println(str);
				} finally {
					in.close();
				}
				in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				try {
					String str;
					while ((str = in.readLine()) != null)
						response.getWriter().println(str);
				} finally {
					in.close();
				}
			}
		} catch (Exception e) {
			response.getWriter().println(args);
			response.getWriter().println("" + e.getMessage());
		} finally {
			p.destroy();
		}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
	/**
	 * renderIdentifier
	 * 
	 * @param host
	 * @param plugin
	 * @param plugin_instance
	 * @param type
	 * @param type_instance
	 * @param ds
	 * @return
	 */
	private String renderIdentifier(String host, String plugin, String plugin_instance, String type, String type_instance, String ds) {
		Set<String> result = Sets.newLinkedHashSet();
		result.add(host);
		result.add(plugin_instance.length() == 0 ? plugin : plugin + "-" + plugin_instance);
		result.add(type_instance.length() == 0 ? type : type + "-" + type_instance);
		result.add(ds);
		return Joiner.on("/").join(result);
	}
	
	/**
	 * renderIdentifiers
	 * 
	 * @param hosts
	 * @param plugin
	 * @param pluginInstances
	 * @param type
	 * @param typeInstances
	 * @param dataSources
	 * @return
	 */
	private String renderIdentifiers(List<String> hosts, String plugin, List<String> pluginInstances, String type, List<String> typeInstances, List<String> dataSources) {
		StringBuilder sb = new StringBuilder();
		sb.append(Joiner.on("+").join(hosts));
		// plugin
		sb.append("/" + plugin);
		String pi = Joiner.on("+").join(pluginInstances);
		if (pi.length() > 0)
			sb.append("-" + pi);
		// type
		sb.append("/" + type);
		String ti = Joiner.on("+").join(typeInstances);
		if (ti.length() > 0)
			sb.append("-" + ti);
		// ds
		if (!ImmutableList.of("value").equals(dataSources))
			sb.append("/" + Joiner.on("+").join(dataSources));
		return sb.toString();
	}
	
	/**
	 * getDataSourceMeta
	 * 
	 * @param type
	 *            type from types.db, e.g., "load"
	 * @return e.g., {"shortterm", "midterm", "longterm"}
	 */
	List<String> getDataSourceMeta(String type) throws Exception {
		List<String> result = new ArrayList<String>();
		final BufferedReader br = new BufferedReader(new FileReader("/usr/share/collectd/types.db"));
		try {
			String line;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				if (st.hasMoreTokens()) {
					String t = st.nextToken().trim();
					if (t.equals(type)) {
						while (st.hasMoreTokens())
							result.add(Iterables.get(Splitter.on(":").split(st.nextToken(",").trim()), 0));
					}
				}
			}

		} finally {
			br.close();
		}
		return result;
	}
	/**
	 * getDataSourceMetaType
	 * 
	 * @param type
	 *            type from types.db, e.g., "load"
	 * @return e.g., "GAUGE"
	 */
  static String getDataSourceMetaType(String type) throws Exception {
    Set<String> result = Sets.newHashSet();
    for (String line : Files.readLines(new File("/usr/share/collectd/types.db"), Charsets.UTF_8)) {
      StringTokenizer st = new StringTokenizer(line);
      if (st.hasMoreTokens()) {
        String t = st.nextToken().trim();
        if (t.equals(type)) {
          while (st.hasMoreTokens())
            result.add(Iterables.get(Splitter.on(":").split(st.nextToken(",").trim()), 1));
        }
      }
    }
    return result.iterator().next();
	}

	static boolean has(String s) {
	  return Strings.nullToEmpty(s).length()>0;
	}
	
	private static final long serialVersionUID = 1L;
	
	private static final List<String> blackTheme = ImmutableList.of("#aaf","#afa","#faa","#aff","#faf","#ffa", "#aaa", "#fff");
}

/*

0.618033988749895
1.61803398875

# use golden ratio
golden_ratio_conjugate = 0.618033988749895
h = rand # use random start value
gen_html {
  h += golden_ratio_conjugate
  h %= 1
  hsv_to_rgb(h, 0.5, 0.95)
}

*/
