'use strict';

angular.module('mockedRecord', [])
    .value('mockRecordJSON', {
        "OAI-PMH": {
            "#text": "\n  \n  \n  \n",
            "@xmlns": "http://www.openarchives.org/OAI/2.0/",
            "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
            "@xsi:schemaLocation": "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd",
            "GetRecord": {
                "#text": "\n    \n  ",
                "record": {
                    "#text": "\n      \n      \n    ",
                    "header": {
                        "#text": "\n        \n        \n        \n        \n        \n        \n        \n      ",
                        "datestamp": "2010-07-28",
                        "identifier": "urn:nbn:de:bsz:14-ds-1229427875176-76287",
                        "setSpec": [
                            "pub-type:article",
                            "has-source-swb:false",
                            "open_access",
                            "ddc:020",
                            "doc-type:article"
                        ]
                    },
                    "metadata": {
                        "#text": "\n        \n      ",
                        "oai_dc:dc": {
                            "#text": "\n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n          \n        ",
                            "@xmlns:oai_dc": "http://www.openarchives.org/OAI/2.0/oai_dc/",
                            "@xsi:schemaLocation": "http://www.openarchives.org/OAI/2.0/oai_dc/\n                http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
                            "dc:contributor": {
                                "#text": "SLUB Dresden, Allgemein",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:creator": {
                                "#text": "Redaktion, BIS ",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:date": {
                                "#text": "2008-12-16",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:description": {
                                "#text": "Autorenliste des Heftes 4 / 2008",
                                "@xml:lang": "deu",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:format": {
                                "#text": "application/pdf",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:identifier": [
                                {
                                    "#text": "http://nbn-resolving.de/urn:nbn:de:bsz:14-ds-1229427875176-76287",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "urn:nbn:de:bsz:14-ds-1229427875176-76287",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "http://www.qucosa.de/fileadmin/data/qucosa/documents/26/1229427875176-7628.pdf",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                }
                            ],
                            "dc:language": {
                                "#text": "deu",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:publisher": {
                                "#text": "Saechsische Landesbibliothek- Staats- und Universitaetsbibliothek Dresden",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:source": {
                                "#text": "BIS - Das Magazin der Bibliotheken in Sachsen 1(2008)4, S. 273",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:subject": [
                                {
                                    "#text": "SLUB Dresden",
                                    "@xml:lang": "deu",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "Sachsen",
                                    "@xml:lang": "deu",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "Bibliotheken",
                                    "@xml:lang": "deu",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "Saxony",
                                    "@xml:lang": "eng",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "Libraries",
                                    "@xml:lang": "eng",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "ddc:020",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                },
                                {
                                    "#text": "rvk:--",
                                    "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                                }
                            ],
                            "dc:title": {
                                "#text": "Autoren",
                                "@xml:lang": "deu",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            },
                            "dc:type": {
                                "#text": "doc-type:article",
                                "@xmlns:dc": "http://purl.org/dc/elements/1.1/"
                            }
                        }
                    }
                }
            },
            "request": {
                "#text": "http://www.qucosa.de/oai/",
                "@identifier": "urn:nbn:de:bsz:14-ds-1229427875176-76287",
                "@metadataPrefix": "oai_dc",
                "@verb": "GetRecord"
            },
            "responseDate": "2013-08-14T10:19:01Z"
        }
    });