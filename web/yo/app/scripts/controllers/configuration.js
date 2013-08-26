'use strict';

angular.module('dmpApp')
  .controller('ConfigurationCtrl', ['$scope', 'PubSub', function ($scope, PubSub) {

    $scope.internalName = 'Configuration Widget';

    $scope.component = {};
    $scope.visibility = 'hide';

    PubSub.subscribe($scope, 'handleEditConfig', function(args) {
      $scope.component = {};
      $scope.data = {};

      $scope.component = args['payload'];

      $scope.visibility = 'show';
    });

    $scope.onSaveClick = function() {
      $scope.component = {};
      $scope.visibility = 'hide';
    };

  }]);
