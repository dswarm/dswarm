'use strict';

angular.module('dmpApp')
  .controller('TransformationCtrl', ['$scope', '$http', 'PubSub', function ($scope, $http, PubSub) {
    $scope.internalName = 'Transformation Logic Widget';

    var allComponents = {}
      , activeComponentId = null
      , availableIds = []
      , makeComponentId = (function () {
          var _id = 0;
          return function () {
            _id += 1;
            return [activeComponentId, 'fun_' + _id].join(':');
          };
        })();

    $scope.showSortable = false;
    $scope.sourceComponent = null;
    $scope.targetComponent = null;
    $scope.components = [];
    $scope.tabs = [];

    function activate(id, skipBackup, skipBroadcast) {
      $scope.showSortable = true;
      if (activeComponentId !== id) {
        $scope.$broadcast('tabSwitch', id);

        if (!skipBackup) {
          allComponents[activeComponentId] = {
            components: $scope.components,
            source: $scope.sourceComponent,
            target: $scope.targetComponent
          };
        }

        var newComponents = allComponents[id];

        $scope.components = newComponents.components;
        $scope.sourceComponent = newComponents.source;
        $scope.targetComponent = newComponents.target;

        activeComponentId = id;

        if (!skipBroadcast) {
          PubSub.broadcast('connectionSwitched', {id: id});
        }
      }
    }

    $scope.switchTab = function(tab) {
      activate(tab.id);
    };

    $scope.sendTransformation = function (tab) {
      var id = tab.id;
      if (activeComponentId === id) {
        var payload = {
            'id': id,
            'name': tab.title,
            'components': angular.copy($scope.components),
            'source': angular.copy($scope.sourceComponent),
            'target': angular.copy($scope.targetComponent)
          }
        , transformations = {
            'transformations': [payload]
          };

        var p = $http.post(
          'http://localhost:8087/dmp/transformations', transformations);

        p.then(function(resp) {
          console.log(resp);
          PubSub.broadcast('transformationFinished', resp.data);
        });
      }

    };

    PubSub.subscribe($scope, 'connectionSelected', function(data) {
      var id = data.id;
      if (activeComponentId !== id) {
        if (allComponents.hasOwnProperty(id)) {
          var idx = availableIds.indexOf(id);
          $scope.tabs[idx].active = true;
        } else {

          var start = {
              componentType: 'source',
              payload: data.sourceData,
              id: data.id + ':source',
              source: data.source,
              target: data.target
            }
            , end = {
              componentType: 'target',
              payload: data.targetData,
              id: data.id + ':source',
              source: data.source,
              target: data.target
            };

          allComponents[id] = {
            components: [],
            source: start,
            target: end
          };
          $scope.tabs.push({title: data.label, active: true, id: id});
          availableIds.push(id);
          activate(id, true, true);
        }
      }
      $scope.$digest();
    });

    var lastPayload;

    function push(data, index, oldIndex) {
      if (angular.isDefined(oldIndex)) {
        $scope.components.splice(oldIndex, 1);
      }
      if (angular.isDefined(index)) {
        $scope.components.splice(index, 0, data);
      } else {
        $scope.components.push(data);
      }
    }

    $scope.sortableCallbacks = {
      receive: function (event, ui) {
        var payload = angular.element(ui.item).scope()['child']
          , componentId = makeComponentId();

        lastPayload = {componentType: 'fun', payload: payload, id: componentId};
      },
      update: function (event, ui) {
        //noinspection JSCheckFunctionSignatures
        var index = ui.item.parent().children('.function').index(ui.item)
          , payload, oldIndex;
        if (lastPayload) {
          payload = angular.copy(lastPayload);
          lastPayload = null;
        } else {
          payload = ui.item.scope()['component'];
          oldIndex = $scope.components.indexOf(payload);
        }

        if (payload) {
          push(payload, index, oldIndex);
          ui.item.remove();
        }

        $scope.$digest();
      }
    };

    $scope.onFunctionClick = function(component) {
      PubSub.broadcast('handleEditConfig', component);
    };

  }]);
