'use strict';

angular.module('dmpApp')
  .controller('ConfigurationCtrl', ['$scope', 'PubSub', function ($scope, PubSub) {

    $scope.internalName = 'Configuration Widget';

    $scope.component = null;

    PubSub.subscribe($scope, 'handleEditConfig', function(args) {
      $scope.component = args['payload'];
    });

    $scope.onSaveClick = function() {
      $scope.component = null;
    };

  }]);
