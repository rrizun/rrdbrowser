var app = angular.module("rrdbrowser", []);

app.factory("stateService", function($http, $timeout) {

  var state = {};

  refresh = function() {
    $http("/state").success(function(data, status, headers, config) {
      state = data;
      $timeout(refresh, 2000);
    });
  };

  refresh(); // kickoff

  return {
    state : function() {
      return state;
    }
  };

});

app.controller("MyController", function($scope, $http, $window) {
  $scope.graphList = [{}];
  $scope.addGraph = function() {
    $scope.graphList.push({});
  }
  $scope.getTheWidth = function() {
    return $window.innerWidth+"px";
  }
  $scope.getTheHeight = function() {
    var height = ($window.innerHeight-96)/$scope.graphList.length;
    return height+"px";
  }
  
});

app.controller("GraphController", function($scope, $http, $window) {

  $scope.$watch("selectedHost", function(newValue) {
    if (newValue) {
      $scope.refreshPlugins();
    }
  });

  $scope.$watch("selectedPlugin", function(newValue) {
    if (newValue) {
      $scope.refreshPluginInstances();
    }
  });

  $scope.$watch("selectedPluginInstance", function(newValue) {
    if (newValue) {
      $scope.refreshTypes();
    }
  });

  $scope.$watch("selectedType", function(newValue) {
    if (newValue) {
      $scope.refreshTypeInstances();
    }
  });
  
  $scope.refreshHosts = function() {
    $http.post("/rrdbrowser/api/getAllHosts", {}).success(function(data) {
      $scope.allHosts = data;
      $scope.selectedHost = data[0];
      $scope.refreshPlugins();
    });
  };

  $scope.refreshPlugins = function() {
    var request = {
      selectedHosts : [ $scope.selectedHost ]
    };
    $http.post("/rrdbrowser/api/getAllPlugins", request).success(function(data) {
      $scope.allPlugins = data;
      $scope.selectedPlugin = data[0];
      $scope.refreshPluginInstances();
    });
  };

  $scope.refreshPluginInstances = function() {
    var request = {
      selectedHosts : [ $scope.selectedHost ],
      selectedPlugin : $scope.selectedPlugin
    };
    $http.post("/rrdbrowser/api/getAllPluginInstances", request).success(function(data) {
      $scope.allPluginInstances = data;
      $scope.selectedPluginInstance = data[0];
      $scope.refreshTypes();
    });
  };

  $scope.refreshTypes = function() {
    var request = {
      selectedHosts : [ $scope.selectedHost ],
      selectedPlugin : $scope.selectedPlugin,
      selectedPluginInstances : [ $scope.selectedPluginInstance ]
    };
    $http.post("/rrdbrowser/api/getAllTypes", request).success(function(data) {
      $scope.allTypes = data;
      $scope.selectedType = data[0];
      $scope.refreshTypeInstances();
    });
  };

  $scope.refreshTypeInstances = function() {
    var request = {
      selectedHosts : [ $scope.selectedHost ],
      selectedPlugin : $scope.selectedPlugin,
      selectedPluginInstances : [ $scope.selectedPluginInstance ],
      selectedType : $scope.selectedType
    };
    $http.post("/rrdbrowser/api/getAllTypeInstances", request).success(function(data) {
      $scope.allTypeInstances = data;
      $scope.selectedTypeInstance = data[0];
//      $scope.refreshDataSources();
    });
  };

  $scope.getImgSrc = function(graphCount) {
    var width = $window.innerWidth;
    var height = ($window.innerHeight-96)/graphCount;

    var src = "rrd?w="+width+"&h="+height;
    
    src += "&host="+$scope.selectedHost;
    
    src += "&p="+$scope.selectedPlugin;
    if ($scope.selectedPluginInstance)
      src += "&pi="+$scope.selectedPluginInstance;
    if ($scope.selectedType)
      src += "&t="+$scope.selectedType;
    if ($scope.selectedTypeInstance)
      src += "&ti="+$scope.selectedTypeInstance;
    
    return src;
  };

  $scope.refreshHosts();
  
});
