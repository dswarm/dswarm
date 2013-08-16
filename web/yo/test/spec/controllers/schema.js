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
      $httpBackend.whenGET('/data/targetschema.json').respond(mockSchemaJSON);

      SchemaCtrl = $controller('SchemaCtrl', {
        $scope: scope
      });

      $httpBackend.flush();

  }));

  it('should have loaded schema data', function () {

      expect(scope.sourceSchema.name).toBe('OAI-PMH');
      expect(scope.sourceSchema.children.length).toBe(3);

      expect(scope.targetSchema.name).toBe('OAI-PMH');
      expect(scope.targetSchema.children.length).toBe(3);

  });
});
