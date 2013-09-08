'use strict';

angular.module('dmpApp')
    .controller('FilterCtrl', ['$scope','$http', 'schemaParser', 'PubSub', function ($scope, $http, schemaParser, PubSub) {

        $scope.internalName = 'Filter Widget';

        $scope.component = null;

        $scope.filterShouldBeOpen = false;

        $scope.result = {};

        $scope.update = function() {

            angular.forEach($scope.component.filters, function(filter, key){
                filter.name = schemaParser.getData(filter.filter).join(',');
            });

            return true;
        }

        $http.get('/data/schema.json')
            .success(function (result) {
                $scope.result = result;
            });

        $scope.opts = {
            backdropFade: true,
            dialogFade:true,
            triggerClass: 'really in'
        };

        $scope.addFilter = function () {
            if(!$scope.component.filters) {
                $scope.component.filters = [];
            }

            $scope.component.filters.push({
                filter : schemaParser.mapData($scope.result['title'], $scope.result, true),
                name : 'new filter'
            });

        }

        $scope.close = function () {
            $scope.filterShouldBeOpen = false;
        };

        PubSub.subscribe($scope, 'handleEditFilter', function(args) {
            $scope.filterShouldBeOpen = true;
            $scope.component = args['payload'];

        });

        $scope.onSaveClick = function() {
            $scope.component = null;
        };

    }]);
