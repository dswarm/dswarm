'use strict';

beforeEach(module('dmpApp'));

describe('Controller: ConfigurationCtrl', function () {

    var configurationCtrl,
        scope,
        element;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
        scope = $rootScope.$new();

        scope.component = {
            "name" : "equals",
            "reference" : "equals",
            "parameters" : {
                "string" :{
                    "type" : "text"
                }
            }
        };

        scope.$digest();

        configurationCtrl = function () {
            return $controller('ConfigurationCtrl', {
                $scope: scope
            });
        };

    }));

    it('should have a ConfigurationCtrl controller', function() {
        var ConfigurationCtrl = configurationCtrl();
        expect(ConfigurationCtrl).not.toBe(null);
    });

});
