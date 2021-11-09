# Data stream resource for Nuvla API server

[![Build Status](https://github.com/nuvla/data-stream-resource/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/nuvla/data-stream-resource/actions/workflows/main.yml)

This repository contains the code for data stream resource of Nuvla API
server, packaged as a Docker container. Each time a release of 
Nuvla API Server is done, this project should also be released
 to produce a container with all changes.

## Artifacts

 - `sixsq/data-stream:<version>`. A Docker container that can be obtained from
   the [nuvla/data-stream repository](https://hub.docker.com/r/nuvla/data-stream)
   in Docker Hub. The tags indicate the release number. This Docker container 
   is a simple extension of [nuvla/api](https://hub.docker.com/r/nuvla/api) Docker
   container with extra set of private jars.

 - `sixsq.nuvla.server/data-stream-jar` JAR file.  This archive can be
   obtained from the production S3 Maven archive that is maintained in
   AWS S3.

## Contributing

### Source Code Changes

To contribute code to this repository, please follow these steps:

 1. Create a branch from master with a descriptive, kebab-cased name
    to hold all your changes.

 2. Follow the developer guidelines concerning formatting, etc. when
    modifying the code.
   
 3. Once the changes are ready to be reviewed, create a GitHub pull
    request.  With the pull request, provide a description of the
    changes and links to any relevant issues (in this repository or
    others). 
   
 4. Ensure that the triggered CI checks all pass.  These are triggered
    automatically with the results shown directly in the pull request.

 5. Once the checks pass, assign the pull request to the repository
    coordinator (who may then assign it to someone else).

 6. Interact with the reviewer to address any comments.

When the reviewer is happy with the pull request, he/she will "squash
& merge" the pull request and delete the corresponding branch.

### Testing

Add appropriate tests that verify the changes or additions you make to
the source code.  For new resources in particular, ensure that you
test the resource schema and provide a lifecycle test.

### Code Formatting

The bulk of the code in this repository is written in Clojure.

The formatting follows the standard formatting provided by the Cursive
IntelliJ plugin with all the default settings **except that map
and let entries should be aligned**.

Additional, formatting guidelines, not handled by the Cursive plugin:

 - Use a new line after the `:require` and `:import` keys in namespace
   declarations.

 - Alphabetize the required namespaces.  This can be automated with
   `lein nsorg --replace`.

 - Use 2 blank lines between top-level forms.

 - Use a single blank line between a block comment and the following
   code.

IntelliJ (with Cursive) can format easily whole directories of source
code.  Do not hesitate to use this feature to keep the source code
formatting standardized.

## Copyright

Copyright &copy; 2021, SixSq SA

## License

Licensed SixSq SA
