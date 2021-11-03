FROM nuvladev/api:master
                
ARG GIT_BRANCH
ARG GIT_COMMIT_ID
ARG GIT_DIRTY
ARG GIT_BUILD_TIME
ARG BUILD_NUMBER
ARG BUILD_WEB_URL

LABEL git.branch=${GIT_BRANCH}
LABEL git.commit.id=${GIT_COMMIT_ID}
LABEL git.dirty=${GIT_DIRTY}
LABEL git.build.time=${GIT_BUILD_TIME}
LABEL ci.build.number=${BUILD_NUMBER}
LABEL ci.build.web.url=${BUILD_WEB_URL}

ADD target/dependency/lib /opt/nuvla/server/lib.d/api
