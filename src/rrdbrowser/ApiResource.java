package rrdbrowser;

import java.util.*;

import javax.ws.rs.*;

import com.google.common.collect.*;

//// hosts
//for (String logicalHost : logicalHosts) {
//List<String> physicalHosts = "*".equals(logicalHost)?RrdTools.getAllHosts():ImmutableList.of(logicalHost);
//// plugin
//// plugin instances
//for (String logicalPluginInstance : plugin_instances) {
//  List<String> physicalPluginInstances = "*".equals(logicalPluginInstance)?RrdTools.getAllPluginInstances(physicalHosts, plugin):ImmutableList.of(logicalPluginInstance);
//  // type
//  // type instances
//  for (String logicalTypeInstance : type_instances) {
//    List<String> physicalTypeInstances = "*".equals(logicalTypeInstance)?RrdTools.getAllTypeInstances(physicalHosts, plugin, physicalPluginInstances, type):ImmutableList.of(logicalTypeInstance);
//    // dataSources
//    for (String param : params) {

/**
 * 
 * @author rrizun
 * 
 */
@Path("/")
public class ApiResource {

  static class Request {
    public final List<String> selectedHosts = Lists.newArrayList();
    public String selectedPlugin;
    public final List<String> selectedPluginInstances = Lists.newArrayList();
    public String selectedType;
    public final List<String> selectedTypeInstances = Lists.newArrayList();
  }

  private List<String> getEffectiveHosts(List<String> selectedHosts) throws Exception {
    List<String> result = Lists.newArrayList();
    for (String logicalHost: selectedHosts) {
      if ("*".equals(logicalHost))
        result.addAll(RrdTools.getAllHosts());
      else
        result.add(logicalHost);
    }
    return result;    
  }

  private List<String> getEffectivePluginInstances(List<String> selectedHosts, String selectedPlugin, List<String> selectedPluginInstances) throws Exception {
    List<String> result = Lists.newArrayList();
    for (String logicalPluginInstance: selectedPluginInstances) {
      if ("*".equals(logicalPluginInstance))
        result.addAll(RrdTools.getAllPluginInstances(getEffectiveHosts(selectedHosts), selectedPlugin));
      else
        result.add(logicalPluginInstance);
    }
    return result;    
  }

  /**
   * getAllHosts
   */
  @POST
  @Path("/getAllHosts")
  public List<String> getAllHosts() throws Exception {
    return RrdTools.getAllHosts();
  }

  /**
   * getAllPlugins
   */
  @POST
  @Path("/getAllPlugins")
  public List<String> getAllPlugins(Request request) throws Exception {
    return RrdTools.getAllPlugins(getEffectiveHosts(request.selectedHosts));
//    List<String> pluginMeta = Lists.newArrayList();
//    for (String logicalHost: request.selectedHosts) {
//      List<String> physicalHosts = "*".equals(logicalHost) ? RrdTools.getAllHosts() : ImmutableList.of(logicalHost);
//      pluginMeta.addAll(RrdTools.getAllPlugins(physicalHosts));
//    }
//    return pluginMeta;
  }

  /**
   * getAllPluginInstances
   */
  @POST
  @Path("/getAllPluginInstances")
  public List<String> getAllPluginInstances(Request request) throws Exception {
    return RrdTools.getAllPluginInstances(getEffectiveHosts(request.selectedHosts), request.selectedPlugin);

//    List<String> pluginInstanceMeta = Lists.newArrayList();
//
////    //###TODO NEED TO WILDCARD-EXPAND REQUEST HOSTS, PLUGINS, ETC...
////    List<String> result = RrdTools.getAllPluginInstances(request.selectedHosts, request.selectedPlugin);
////    if (!ImmutableList.of("").equals(result))
////      result.add(0, "*");
//
//    for (String logicalHost: request.selectedHosts) {
//      List<String> physicalHosts = "*".equals(logicalHost) ? RrdTools.getAllHosts() : ImmutableList.of(logicalHost);
//      pluginInstanceMeta.addAll(RrdTools.getAllPluginInstances(physicalHosts, request.selectedPlugin));
//    }
//
//    return pluginInstanceMeta;
  }

  /**
   * getAllTypes
   */
  @POST
  @Path("/getAllTypes")
  public List<String> getAllTypes(Request request) throws Exception {
    
    List<String> physicalHosts = getEffectiveHosts(request.selectedHosts);
    String physicalPlugin = request.selectedPlugin;
    List<String> physicalPluginInstances = getEffectivePluginInstances(request.selectedHosts, request.selectedPlugin, request.selectedPluginInstances);

    return RrdTools.getAllTypes(physicalHosts, physicalPlugin, physicalPluginInstances);
    
//    List<String> result = Lists.newArrayList();
//    
//    List<String> physicalHosts = getEffectiveHosts(request.selectedHosts);
//    String physicalPlugin = request.selectedPlugin;
//    List<String> physicalPluginInstances = getEffectivePluginInstances(physicalHosts, physicalPlugin);
//
//    result.addAll(RrdTools.getAllTypes(physicalHosts, physicalPlugin, physicalPluginInstances);
    
//    //    return RrdTools.getAllTypes(request.selectedHosts, request.selectedPlugin, request.selectedPluginInstances);
//
//    
//    for (String logicalHost: request.selectedHosts) {
//      List<String> physicalHosts = "*".equals(logicalHost) ? RrdTools.getAllHosts() : ImmutableList.of(logicalHost);
//      String physicalPlugin = request.selectedPlugin;
//      for (String logicalPluginInstance: request.selectedPluginInstances) {
//        List<String> physicalPluginInstances = "*".equals(logicalPluginInstance) ? RrdTools.getAllHosts() : ImmutableList.of(logicalPluginInstance);
//        result.addAll(RrdTools.getAllTypes(physicalHosts, physicalPlugin,  physicalPluginInstances));
//      }
//    }

//    return result;
  }

  /**
   * getAllTypeInstances
   */
  @POST
  @Path("/getAllTypeInstances")
  public List<String> getAllTypeInstances(Request request) throws Exception {
    List<String> physicalHosts = getEffectiveHosts(request.selectedHosts);
    String physicalPlugin = request.selectedPlugin;
    List<String> physicalPluginInstances = getEffectivePluginInstances(request.selectedHosts, request.selectedPlugin, request.selectedPluginInstances);
    String physicalType = request.selectedType;

    return RrdTools.getAllTypeInstances(physicalHosts, physicalPlugin, physicalPluginInstances, physicalType);
  }
}
