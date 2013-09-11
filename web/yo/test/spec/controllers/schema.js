'use strict';

beforeEach(module('dmpApp', 'mockedSchema'));

describe('Controller: SchemaCtrl', function () {

  var schemaCtrl,
    scope,
    $httpBackend;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, _$httpBackend_, $rootScope, mockSchemaJSON) {
      scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
      $httpBackend.whenGET('/data/schema.json').respond(mockSchemaJSON);

      schemaCtrl = function() {
        return $controller('SchemaCtrl', {
          '$scope': scope
        });
      };
    }
  ));

  afterEach(inject(function () {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  }));

  it('should have loaded schema data', inject(function () {
      $httpBackend.expectGET('/data/schema.json');
      schemaCtrl();
      $httpBackend.flush();

      expect(scope.data.name).toBe('OAI-PMH');

      expect(scope.data.children.length).toBe(3);

    }
  ));
});
