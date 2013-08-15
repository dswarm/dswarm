'use strict';

beforeEach(module('dmpApp', 'mockedSchema'));

describe('Controller: SchemaCtrl', function () {

  var SchemaCtrl,
    scope,
    mockedSchema;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $httpBackend, $rootScope, mockSchemaJSON) {
      scope = $rootScope.$new();

      $httpBackend.whenGET('/data/schema.json').respond(mockSchemaJSON);

      SchemaCtrl = $controller('SchemaCtrl', {
        $scope: scope
      });

      $httpBackend.flush();

  }));

  it('should have loaded schema data', function () {

      expect(scope.data.name).toBe('OAI-PMH');

      expect(scope.data.children.length).toBe(3);

  });
});
