'use strict';

beforeEach(module('dmpApp', 'mockedSchemaParsed'));

describe('Directive: tree', function() {

    var element,
        scope;

    beforeEach(module('views/directives/tree.html'));

    beforeEach(inject(function($rootScope, $compile, mockSchemaParsedJSON) {
        element = angular.element('<tree data="data"></tree>');

        scope = $rootScope;

        scope.data = mockSchemaParsedJSON;

        $compile(element)(scope);
        scope.$digest();
    }));

    it("should have the correct amount of tree nodes", function() {
        var list = element.find('tree');
        expect(list.length).toBe(33);
    });
});