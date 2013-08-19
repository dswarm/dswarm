'use strict';

angular.module('dmpApp')
  .controller('TransformationCtrl', ['$scope', function ($scope) {
    $scope.internalName = 'Transformation Logic Widget';

    $scope.components = [];


    $scope.droppableOptions = {
      accept: '.tree-leaf',
      activeClass: 'ui-state-hover',
      hoverClass: 'ui-state-active',
      drop: function(event, ui) {
        $scope.components.push(ui.helper.data());
        $scope.$digest();
      }

    };

  }]);
