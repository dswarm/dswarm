#!/usr/bin/env python

from __future__ import print_function

import argparse
import httplib
import os
import sys

from os.path import join, realpath, dirname
from subprocess import Popen, PIPE
from urlparse import urlparse


def reset_mysql(username, password, database, persistence_module, verbose):
    command = ["mysql", "-f", "-u{}".format(username), "-p{}".format(password), database]

    def execute(file):
        with open(file, 'rb') as fh:
            if verbose:
                print('calling "{} < {}"'.format(' '.join(command), file))
            process = Popen(command, stdin=fh, stdout=PIPE, stderr=PIPE)
            (out, err) =  process.communicate()
            return (out, err, process.returncode)

    files = ["schema.sql", "functions.sql", "init_internal_schema.sql"]
    files = map(lambda f: join(persistence_module, 'src', 'main', 'resources', f), files)

    return (execute(file) for file in files)


def reset_graph(neo4j_endpoint, verbose):
    neo4j = neo4j_endpoint._replace(path=neo4j_endpoint.path + '/maintain/delete')

    if verbose:
        print('doing "curl -XDELTE {}"'.format(neo4j.geturl()))

    conn = httplib.HTTPConnection(neo4j.netloc)
    conn.request('DELETE', neo4j.path)
    resp = conn.getresponse()
    content = resp.read()
    return content


class DirType(object):

    def __init__(self, mode='r'):
        self._access = os.W_OK if mode is 'w' else os.R_OK
        self._description = 'writable' if mode is 'w' else 'readable'

    def __call__(self, path):
        if not os.path.isdir(path):
            raise argparse.ArgumentTypeError('argument "%s" is not a directory' % path)
        if not os.access(path, self._access):
            raise argparse.ArgumentTypeError('argument "%s" is not %' % (path, self._description))

        return realpath(path)

    def __repr__(self):
        return '%s directory' % self._description


def main(arguments):
    exit_code = 0

    default_module = join(dirname(dirname(dirname(realpath(__file__)))), 'data-management-platform', 'persistence')
    persistence_module = arguments.persistence_module or DirType('r')(default_module)

    if not arguments.no_mysql:
        if arguments.verbose:
            print("resetting MySQL")
        for (out, err, ret) in reset_mysql(arguments.user, arguments.password, arguments.db, persistence_module, arguments.verbose):
            exit_code += ret
            print(out, file=sys.stdout, end='')
            print(err, file=sys.stderr, end='')
    else:
        if arguments.verbose:
            print("skipping MySQL")

    if not arguments.no_neo4j:
        if arguments.verbose:
            print("resetting Neo4j")
        neo4j = urlparse(arguments.neo4j)
        try:
            print(reset_graph(neo4j, arguments.verbose))
        except Exception as e:
            print(e, file=sys.stderr)
            exit_code += 1
    else:
        if arguments.verbose:
            print("skipping Neo4j")

    return exit_code

def arguments():
    default_neo = 'http://localhost:7474/graph'

    parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('-v', '--verbose', help='be chattier', action='store_true')
    parser.add_argument('--version', action='version', version='%(prog)s 2.0')
    parser.add_argument('-m', '--persistence-module', help='the location of the persistence module', type=DirType('r'))
    parser.add_argument('-u', '--user', default='dmp', help='MySQL username')
    parser.add_argument('-p', '--password', default='dmp', help='MySQL password')
    parser.add_argument('-d', '--db', default='dmp', help='MySQL database')
    parser.add_argument('--no-mysql', action='store_true', help='skip resetting of MySQL')
    parser.add_argument('-n', '--neo4j', default=default_neo, help='Endpoint to our Neo4j Graph extension')
    parser.add_argument('--no-neo4j', action='store_true', help='skip resetting of Neo4j')

    return parser.parse_args()


if __name__ == "__main__":
    sys.exit(main(arguments()))
