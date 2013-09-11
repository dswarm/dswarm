'use strict';

angular.module('mockedSchemaParsed', [])
    .value('mockSchemaParsedJSON', {
        "name": "OAI-PMH",
        "show": true,
        "children": [
            {
                "name": "GetRecord",
                "show": true,
                "children": [
                    {
                        "name": "record",
                        "show": true,
                        "children": [
                            {
                                "name": "header",
                                "show": true,
                                "children": [
                                    {
                                        "name": "identifier",
                                        "show": true
                                    },
                                    {
                                        "name": "datestamp",
                                        "show": true
                                    },
                                    {
                                        "name": "setSpec",
                                        "show": true
                                    }
                                ]
                            },
                            {
                                "name": "metadata",
                                "show": true,
                                "children": [
                                    {
                                        "name": "oai_dc:dc",
                                        "show": true,
                                        "children": [
                                            {
                                                "name": "dc:title",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:creator",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:subject",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:description",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:publisher",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:contributor",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:date",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:type",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:format",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:identifier",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:source",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:language",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:relation",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:coverage",
                                                "show": true
                                            },
                                            {
                                                "name": "dc:rights",
                                                "show": true
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "name": "about",
                                "show": true
                            }
                        ]
                    }
                ]
            },
            {
                "name": "request",
                "show": true,
                "children": [
                    {
                        "name": "@verb",
                        "show": true
                    },
                    {
                        "name": "@identifier",
                        "show": true
                    },
                    {
                        "name": "@metadataPrefix",
                        "show": true
                    },
                    {
                        "name": "@from",
                        "show": true
                    },
                    {
                        "name": "@until",
                        "show": true
                    },
                    {
                        "name": "@set",
                        "show": true
                    },
                    {
                        "name": "@resumptionToken",
                        "show": true
                    }
                ]
            },
            {
                "name": "responseDate",
                "show": true
            }
        ]
    });