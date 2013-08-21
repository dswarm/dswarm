'use strict';

angular.module('dmpApp')
  .controller('ConfigurationCtrl', ['$scope', function ($scope) {
        $scope.internalName = 'Configuration Widget';

        $scope.component = {};
        $scope.visibility = "hide";

        $scope.$on('handleEditConfig', function(event, args) {

            $scope.component = {};
            $scope.data = {};

            $scope.component = args['payload'];

            $scope.visibility = "show";

        });

        $scope.onSaveClick = function() {
            $scope.component = {};
            $scope.visibility = "hide";
        }

  }]);
