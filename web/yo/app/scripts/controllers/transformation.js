'use strict';

angular.module('dmpApp')
  .controller('TransformationCtrl', ['$scope', function ($scope) {
    $scope.internalName = 'Transformation Logic Widget';

    $scope.components = [];

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

    var lastPayload = null;

    $scope.sortableOptions = {
      tolerance: 'pointer',
      items: '.functionDraggable',
      revert: 250,
      snap: true,
      forcePlaceholderSize: true,
      containment: '#transformation',
      cursor: 'move',
      cursorAt: {top: -5, left: -5},
      opacity: 0.7,

      receive: function(event, ui) {
        var payload = angular.element(ui.item).scope()['child'];
        lastPayload = {componentType: 'fun', payload: payload};
      },
      update: function(event, ui) {
        var index = ui.item.parent().children().index(ui.item)
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
  }]);
