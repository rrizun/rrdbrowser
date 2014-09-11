package rrdbrowser;

import java.io.*;
import java.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

public class RrdTools {

  /*
   * 
   */
  static String getRrdRoot() {
    String root = "/var/lib/collectd/rrd";
    if (new File(root).exists())
      return root;
    return "/var/lib/collectd";
  }

  /*
   * 
   */
  static String instancePart(final String s) {
    String result = "";
    int index = s.indexOf("-");
    if (index != -1)
      result = s.substring(1 + index);
    if (result.endsWith(".rrd"))
      result = result.substring(0, result.length() - ".rrd".length());
    return result;
  }
  
  /**
   * getAllHosts
   * 
   * @return
   */
  public static List<String> getAllHosts() {
    Set<String> result = Sets.newTreeSet();
    String[] list = new File(getRrdRoot()).list();
    if (list != null) {
      for (String host : list)
        result.add(host);
    }
    return Lists.newArrayList(result);
  }
  
  /**
   * getAllPlugins
   * 
   * @param selectedHosts
   * @return
   * @throws Exception
   */
  public static List<String> getAllPlugins(List<String> selectedHosts) throws Exception {
    Set<String> result = Sets.newTreeSet();
    for (String host : selectedHosts) {
      File hostPath = new File(getRrdRoot() + "/" + host);
      if (hostPath != null) {
        String[] list = hostPath.list();
        if (list != null)
          for (String pluginAndPluginInstance : list)
            result.add(Iterables.get(Splitter.on("-").split(pluginAndPluginInstance), 0));
      }
    }
    return Lists.newArrayList(result);
  }
  
  /**
   * getAllPluginInstances
   * 
   * @param selectedHosts
   * @param selectedPlugin
   * @return
   * @throws Exception
   */
  public static List<String> getAllPluginInstances(List<String> selectedHosts, String selectedPlugin) throws Exception {
    Set<String> result = Sets.newTreeSet();
    for (String host : selectedHosts) {
      String[] list = new File(getRrdRoot() + "/" + host).list();
      if (list != null)
        for (String pluginAndPluginInstance : list) {
          String plugin = Iterables.get(Splitter.on("-").split(pluginAndPluginInstance), 0);
          if (selectedPlugin.equals(plugin))
            result.add(instancePart(pluginAndPluginInstance));
        }
    }
    return Lists.newArrayList(result);
  }
  
  /**
   * getAllTypes
   * 
   * @param selectedHosts
   * @param selectedPlugin
   * @param selectedPluginInstances
   * @return
   * @throws Exception
   */
  public static List<String> getAllTypes(List<String> selectedHosts, String selectedPlugin, List<String> pluginInstances) throws Exception {
    Set<String> result = Sets.newTreeSet();
    for (String host : selectedHosts) {
      for (String pluginInstance : pluginInstances) {
        String rrd = getRrdRoot() + "/" + host + "/" + selectedPlugin;
        if (!"".equals(pluginInstance))
          rrd += "-" + pluginInstance;
        String[] list = new File(rrd).list(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(".rrd");
          }
        });
        if (list != null) {
          for (String typeAndTypeInstance : list)
            result.add(Iterables.get(Splitter.on("-").split(typeAndTypeInstance.substring(0, typeAndTypeInstance.length() - ".rrd".length())), 0));
        }
      }
    }
    return Lists.newArrayList(result);
  }
  
  /**
   * getAllTypeInstances
   * 
   * @param selectedHosts
   * @param selectedPlugin
   * @param selectedPluginInstances
   * @param selectedType
   * @return
   * @throws Exception
   */
  public static List<String> getAllTypeInstances(List<String> selectedHosts, String selectedPlugin, List<String> selectedPluginInstances, String selectedType) throws Exception {
    Set<String> result = Sets.newTreeSet();
    for (String physicalHost : selectedHosts) {
      for (String pluginInstance : selectedPluginInstances) {
        String rrd = getRrdRoot() + "/" + physicalHost + "/" + selectedPlugin;
        if (!"".equals(pluginInstance))
          rrd += "-" + pluginInstance;
        String[] list = new File(rrd).list(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(".rrd");
          }
        });
        if (list != null) {
          for (String typeAndTypeInstance : list) {
            String type = Iterables.get(Splitter.on("-").split(typeAndTypeInstance.substring(0, typeAndTypeInstance.length() - ".rrd".length())), 0);
            if (selectedType.equals(type))
              result.add(instancePart(typeAndTypeInstance));
          }
        }
      }
    }
    return Lists.newArrayList(result);
  }
  
  public static void main(String[] args) throws Exception {
//    out.println(RrdTools.getAllHosts());
    for (String plugin : RrdTools.getAllPlugins(RrdTools.getAllHosts())) {
      for (String pluginInstance : RrdTools.getAllPluginInstances(RrdTools.getAllHosts(), plugin)) {
        out.println(" p="+(plugin)+" pi="+(pluginInstance)+" -> "+RrdTools.getAllTypes(RrdTools.getAllHosts(), plugin, Lists.newArrayList(pluginInstance)));
      }
//      for (String type : RrdTools.getAllTypes(plugin))
//        out.println(" #p="+plugin+" #t="+type+"="+RrdTools.getAllTypeInstances(plugin, type));
    }
  }
  
  static PrintStream out = System.out;
}
