'use strict';

angular.module('dmpApp')
  .controller('ConfigurationCtrl', ['$scope', 'PubSub', function ($scope, PubSub) {

    $scope.internalName = 'Configuration Widget';

    $scope.component = null;

    $scope.getPattern = function (pattern) {
      return pattern? new RegExp('^' + pattern + '$') : /.*/;
    };

    $scope.formClasses = function (input, isOptional) {
      return {
        'has-error': input.$invalid,
        'has-success': !isOptional && input.$valid
      };
    };

    PubSub.subscribe($scope, 'handleEditConfig', function(args) {
      $scope.component = args['payload'];
    });

    $scope.onSaveClick = function() {
      $scope.component = null;
    };

  }]);
