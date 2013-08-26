'use strict';

angular.module('dmpApp')
  .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('main', {
        url: '/',
        views: {
          'sourceData': {
            templateUrl: 'views/source-data.html',
            controller: 'SourceDataCtrl'
          },
          'schema': {
            templateUrl: 'views/schema.html',
            controller: 'SchemaCtrl'
          },
          'targetData': {
            templateUrl: 'views/target-data.html',
            controller: 'TargetDataCtrl'
          },
          'configuration': {
            templateUrl: 'views/configuration.html',
            controller: 'ConfigurationCtrl'
          },
          'transformation': {
            templateUrl: 'views/transformation.html',
            controller: 'TransformationCtrl'
          },
          'components': {
            templateUrl: 'views/components.html',
            controller: 'ComponentsCtrl'
          }
        }
      });
  }])
  .run(function($rootScope) {
    $rootScope.$on('editConfig', function(event, args) {
      $rootScope.$broadcast('handleEditConfig', args);
    });
  });
